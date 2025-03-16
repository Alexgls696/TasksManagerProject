package org.example.taskservice.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class User {
    private int id;
    private String name;
    private String surname;

    private String username;
    private String password;

    private String role;
    private String email;

}
