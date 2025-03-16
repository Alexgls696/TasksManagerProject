package com.example.tasksmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Task {
    private int id;

    private String title;

    private String description;

    private TaskStatus status;

    private int priority;

    private LocalDateTime deadline;

    private LocalDateTime startDate;

    private LocalDateTime updateDate;

    private Category category;

    private User assignee; //Кому назначена задача

    private User creator;

    private Project project;

}
