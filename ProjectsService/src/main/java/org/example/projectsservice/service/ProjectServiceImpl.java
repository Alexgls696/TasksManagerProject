package org.example.projectsservice.service;

import lombok.RequiredArgsConstructor;

import org.example.projectsservice.client.UsersRestClient;
import org.example.projectsservice.controller.payload.NewProjectPayload;
import org.example.projectsservice.controller.payload.UpdateProjectPayload;
import org.example.projectsservice.entity.Project;
import org.example.projectsservice.entity.ProjectMembers;
import org.example.projectsservice.entity.User;
import org.example.projectsservice.exception.NoSuchProjectException;
import org.example.projectsservice.repository.ProjectMembersRepository;
import org.example.projectsservice.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMembersRepository projectMembersRepository;
    private final UsersRestClient usersRestClient;

    @Override
    public Iterable<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public Optional<Project> findById(int id) {
        Optional<Project> project = projectRepository.findById(id);
        Set<Integer> membersIds = projectMembersRepository.findAllUsersIdByProjectId(id);
        project.get().setMemberIds(membersIds);
        return project;
    }

    @Override
    @Transactional
    public Project save(NewProjectPayload payload) {
        User creator = usersRestClient.findUserById(payload.creatorId())
                .orElseThrow(() -> new NoSuchElementException("User with id " + payload.creatorId() + "not found."));
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
        projectMembersRepository.findById(projectId);

        payload.membersId()
                .forEach(id->{
                    ProjectMembers projectMembers = new ProjectMembers(project, id);
                    projectMembersRepository.save(projectMembers);
                });
        return projectRepository.save(project);
    }

    @Override
    public void deleteById(int id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
        } else {
            throw new NoSuchProjectException("Project with id " + id + " does not exist");
        }
    }
}
