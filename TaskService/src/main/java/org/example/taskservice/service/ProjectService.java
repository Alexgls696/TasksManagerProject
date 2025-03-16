package org.example.taskservice.service;

import org.example.taskservice.entity.Project;

public interface ProjectService {
    Iterable<Project> findAll();
}
