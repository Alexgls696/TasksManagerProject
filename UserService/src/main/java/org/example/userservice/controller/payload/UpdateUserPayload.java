package org.example.userservice.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.userservice.validation.EmailValidation;

public record UpdateUserPayload(
        @NotBlank(message = "{validation.errors.name_is_blank}")
        @Size(max = 50,message = "{validation.errors.name_is_too_big}")
        String name,

        @NotBlank(message = "{validation.errors.surname_is_blank}")
        @Size(max = 50,message = "{validation.errors.surname_is_too_big}")
        String surname,

        @NotBlank(message = "{validation.errors.email_is_blank}")
        @EmailValidation(message = "{validation.errors.email_is_invalid}")
        String email
) {
}
