package org.example.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class EmailValidator implements ConstraintValidator<EmailValidation,String>{

    @Value("${email.validation.list}")
    private List<String> ends;

    @Override
    public void initialize(EmailValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return ends
                .stream()
                .anyMatch(s::endsWith);
    }
}
