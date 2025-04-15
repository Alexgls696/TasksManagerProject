package org.example.securityservice.controller.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreatedUserPayload {
    private String name;
    private String surname;
    private String username;
    private String email;
    private String keycloakId;

    public CreatedUserPayload(NewUserPayload payload, String keycloakId) {
        this.email = payload.email();
        this.name = payload.firstName();
        this.surname = payload.lastName();
        this.username = payload.username().toLowerCase();
        this.keycloakId = keycloakId;
    }

}
