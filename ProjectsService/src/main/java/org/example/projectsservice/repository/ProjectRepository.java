package org.example.projectsservice.repository;


import org.example.projectsservice.entity.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project,Integer> {
    @Query(nativeQuery = true,value = "select * from getprojectbyuserid(:memberId)")
    Iterable<Project>findAllByMemberId(@Param("memberId")int memberId);
}
