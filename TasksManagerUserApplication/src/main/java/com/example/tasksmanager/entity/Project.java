package com.example.tasksmanager.entity;


import java.time.LocalDateTime;
import java.util.Set;


public class Project {

    private int id;
    private String name;
    private String description;

    private User creator;

    private LocalDateTime creationDate;

    private LocalDateTime deadline;

    private ProjectStatus status;

    private Set<User> members;
}
