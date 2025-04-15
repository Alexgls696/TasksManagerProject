package org.example.projectsservice.entity;

import jakarta.persistence.Column;
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
    private String email;
    private String keycloakId;
    private Long createdTimestamp;
}
