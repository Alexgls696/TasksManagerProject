package org.example.taskservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
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

    @JoinColumn(name = "creator_id")
    @OneToOne
    private User creator;

    private LocalDateTime creationDate;

    private LocalDateTime deadline;

    @JoinColumn(name = "status_id")
    @OneToOne
    private ProjectStatus status;

    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @ManyToMany
    private Set<User> members;

}
