package org.example.taskservice.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@ToString
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
