package org.example.projectsservice.repository;

import org.example.projectsservice.entity.ProjectMembers;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProjectMembersRepository extends CrudRepository<ProjectMembers, Integer> {
    List<ProjectMembers> findAllByProjectId(int projectId);
    void deleteByProjectId(int projectId);

    @Query(nativeQuery = true,value = "select user_id from project_members where project_id = :projectId")
    Set<Integer> findAllUsersIdByProjectId(@Param("projectId") int projectId);
}
