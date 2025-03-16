package com.example.tasksmanager.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@ToString
public class TaskStatus {

    private int id;
    private String status;

    public TaskStatus() {
        id = 1;
        status = "Создан";
    }

}
