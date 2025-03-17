package org.example.projectsservice.repository;

import org.example.projectsservice.entity.ProjectStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectStatusRepository extends CrudRepository<ProjectStatus, Integer> {
}
