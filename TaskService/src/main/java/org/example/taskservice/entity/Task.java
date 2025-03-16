package org.example.taskservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.taskservice.controller.payload.NewTaskPayload;
import org.example.taskservice.controller.payload.UpdateTaskPayload;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    private String description;

    @JoinColumn(name = "status_id")
    @OneToOne(cascade = CascadeType.MERGE)
    private TaskStatus status;

    private int priority;

    private LocalDateTime deadline;

    private LocalDateTime startDate;

    private LocalDateTime updateDate;

    @JoinColumn(name = "category_id")
    @OneToOne(cascade = CascadeType.MERGE)
    private Category category;

    @JoinColumn(name = "assignee_id")
    @OneToOne(cascade = CascadeType.MERGE)
    private User assignee; //Кому назначена задача

    @JoinColumn(name = "creator_id")
    @OneToOne(cascade = CascadeType.MERGE)
    private User creator;

    @JoinColumn(name = "project_id")
    @OneToOne(cascade = CascadeType.MERGE)
    private Project project;

    public Task(NewTaskPayload payload) {
        this.title = payload.title();
        this.description = payload.description();
        this.priority = payload.priority();
        this.deadline = payload.deadline();
        this.startDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    public void update(UpdateTaskPayload payload) {
        this.title = payload.title();
        this.description = payload.description();
        this.priority = payload.priority();
        this.deadline = payload.deadline();
        this.updateDate = LocalDateTime.now();
    }
}
