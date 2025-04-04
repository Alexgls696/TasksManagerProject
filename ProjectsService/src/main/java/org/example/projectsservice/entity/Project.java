package org.example.projectsservice.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.projectsservice.controller.payload.GetUserPayload;
import org.example.projectsservice.controller.payload.NewProjectPayload;
import org.example.projectsservice.controller.payload.UpdateProjectPayload;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String description;

    @Column(name = "creator_id", nullable = false)
    private Integer creatorId; // ID создателя (пользователя)

    private LocalDateTime creationDate;
    private LocalDateTime deadline;

    @Column(name = "status_id")
    private Integer statusId;

    @Transient
    private GetUserPayload creator;

    @Transient
    private ProjectStatus projectStatus; // ID статуса проекта

    @Transient
    private Set<Integer> memberIds = new HashSet<>();

    public Project(NewProjectPayload payload){
        name= payload.name();
        description= payload.description();
        creatorId= payload.creatorId();
        deadline= payload.deadline();
        creationDate=LocalDateTime.now();
        statusId=1;
    }

    public void update(UpdateProjectPayload payload){
        name= payload.name();
        description= payload.description();
        deadline= payload.deadline();
        projectStatus = payload.status();
        statusId = payload.status().getId();
    }

}