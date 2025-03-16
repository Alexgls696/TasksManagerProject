package org.example.projectsservice.exception;

public class NoSuchProjectException extends RuntimeException {
    public NoSuchProjectException(String message) {
        super(message);
    }
}
