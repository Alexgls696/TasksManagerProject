package org.example.projectsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "project_members")
@Getter
@Setter
@NoArgsConstructor
public class ProjectMembers {
    @EmbeddedId
    private ProjectMembersId id;

    @ManyToOne
    @MapsId("projectId") // Связывает projectId из ProjectMembersId с project
    @JoinColumn(name = "project_id")
    private Project project;


    public ProjectMembers(Project project, Integer userId) {
        this.id = new ProjectMembersId(project.getId(), userId);
        this.project = project;
    }
}

