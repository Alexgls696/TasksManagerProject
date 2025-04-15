package org.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.userservice.controller.payload.NewUserPayload;
import org.example.userservice.controller.payload.UpdateUserPayload;


@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String surname;
    private String username;
    private String email;

    @Column(name = "keycloak_id")
    private String keycloakId;
    @Column(name = "created_timestamp")
    private Long createdTimestamp;

    public User(NewUserPayload payload) {
        this.email = payload.email();
        this.name = payload.name();
        this.surname = payload.surname();
        this.username = payload.username();
        this.keycloakId = payload.keycloakId();
    }

    public void update(UpdateUserPayload payload) {
        this.name = payload.name();
        this.surname = payload.surname();
        this.email = payload.email();
    }

}
