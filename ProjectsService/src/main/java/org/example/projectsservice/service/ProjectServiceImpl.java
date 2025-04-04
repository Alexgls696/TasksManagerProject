package org.example.projectsservice.service;

import lombok.RequiredArgsConstructor;

import org.example.projectsservice.client.UsersRestClient;
import org.example.projectsservice.controller.payload.GetUserPayload;
import org.example.projectsservice.controller.payload.NewProjectPayload;
import org.example.projectsservice.controller.payload.UpdateProjectPayload;
import org.example.projectsservice.entity.Project;
import org.example.projectsservice.entity.ProjectMembers;
import org.example.projectsservice.entity.ProjectStatus;
import org.example.projectsservice.entity.User;
import org.example.projectsservice.exception.NoSuchProjectException;
import org.example.projectsservice.repository.ProjectMembersRepository;
import org.example.projectsservice.repository.ProjectRepository;
import org.example.projectsservice.repository.ProjectStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMembersRepository projectMembersRepository;
    private final UsersRestClient usersRestClient;
    private final ProjectStatusRepository projectStatusRepository;


    @Override
    public Iterable<Project> findAll() {
        Iterable<Project> projects = projectRepository.findAll();
        return StreamSupport.stream(projects.spliterator(), false)
                .peek(project -> {
                    var user = usersRestClient.findUserById(project.getCreatorId())
                            .orElseThrow(() -> new NoSuchElementException("User with id " + project.getCreatorId() + " not found"));
                    project.setCreator(new GetUserPayload(user.getName(), user.getSurname()));
                    project.setProjectStatus(projectStatusRepository.findById(project.getStatusId()).orElseThrow(() -> new NoSuchElementException("Project status not found")));
                }).collect(Collectors.toList());
    }

    @Override
    public Optional<Project> findById(int id) {
        Optional<Project> project = projectRepository.findById(id);
        Set<Integer> membersIds = projectMembersRepository.findAllUsersIdByProjectId(id);
        project.get().setMemberIds(membersIds);
        project.get().setProjectStatus(projectStatusRepository.findById(project.get().getStatusId()).orElseThrow(() -> new NoSuchElementException("Project status not found")));
        return project;
    }

    @Override
    @Transactional
    public Project save(NewProjectPayload payload) {
        final Project project = projectRepository.save(new Project(payload));
        payload.membersId()
                .forEach(memberId -> {
                    ProjectMembers projectMembers = new ProjectMembers(project, memberId);
                    projectMembersRepository.save(projectMembers);
                });
        return project;
    }

    @Override
    @Transactional
    public Project update(int projectId, UpdateProjectPayload payload) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchProjectException("Project with id " + projectId + " not found."));

        project.update(payload);
        projectMembersRepository.deleteByProjectId(projectId);

        payload.membersId()
                .forEach(id -> {
                    ProjectMembers projectMembers = new ProjectMembers(project, id);
                    projectMembersRepository.save(projectMembers);
                });
        return projectRepository.save(project);
    }

    @Transactional
    @Override
    public void deleteById(int id) {
        if (projectRepository.existsById(id)) {
            projectMembersRepository.deleteByProjectId(id);
            projectRepository.deleteById(id);
        } else {
            throw new NoSuchProjectException("Project with id " + id + " does not exist");
        }
    }
}
