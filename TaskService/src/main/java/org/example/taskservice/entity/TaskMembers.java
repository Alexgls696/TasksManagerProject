package org.example.taskservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks_members")
@NoArgsConstructor
@Getter
@Setter
public class TaskMembers {
    @EmbeddedId
    private TaskMembersId id;

    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(name = "project_id")
    private Task task;

    public TaskMembers(Task task, Integer userId) {
        this.id = new TaskMembersId(task.getId(), userId);
        this.task = task;
    }
}
