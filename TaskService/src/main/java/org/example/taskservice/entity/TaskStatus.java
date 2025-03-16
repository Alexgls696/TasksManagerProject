package org.example.taskservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "statuses")
@Getter
@Setter
@AllArgsConstructor
@ToString
public class TaskStatus {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    private String status;

    public TaskStatus() {
        id = 1;
        status = "Создан";
    }

}
