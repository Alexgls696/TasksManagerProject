package org.example.projectsservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@ToString
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

    @JoinColumn(name = "status_id")
    @OneToOne
    private ProjectStatus projectStatus; // ID статуса проекта

    // Список ID участников (не используем @ElementCollection)
    @Transient // Поле не сохраняется в БД
    private Set<Long> memberIds = new HashSet<>();

}