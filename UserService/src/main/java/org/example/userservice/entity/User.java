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
    //private

    public User(NewUserPayload payload){
        this.email = payload.email();
        this.name = payload.name();
        this.surname = payload.surname();
        this.username = payload.username();
    }

    public void update(UpdateUserPayload payload){
        this.name = payload.name();
        this.surname = payload.surname();
        this.email = payload.email();
    }

}
