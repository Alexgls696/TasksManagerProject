package org.example.projectsservice.repository;


import org.example.projectsservice.entity.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends CrudRepository<Project,Integer> {
    @Query(nativeQuery = true,value = "select * from getprojectsbymemberid(:memberId)")
    List<Project> findAllByMemberId(@Param("memberId")int memberId);
}
