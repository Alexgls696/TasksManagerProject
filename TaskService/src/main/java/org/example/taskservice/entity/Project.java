package org.example.taskservice.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private int id;

    private String name;
    private String description;

    private Integer creatorId; // ID создателя (пользователя)

    private LocalDateTime creationDate;
    private LocalDateTime deadline;

    private ProjectStatus projectStatus; // ID статуса проекта

    private Set<Integer> memberIds = new HashSet<>();
}
