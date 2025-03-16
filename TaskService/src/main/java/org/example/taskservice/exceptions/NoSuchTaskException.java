package org.example.taskservice.exceptions;

public class NoSuchTaskException extends RuntimeException {
    public NoSuchTaskException(String message) {
        super(message);
    }
}
