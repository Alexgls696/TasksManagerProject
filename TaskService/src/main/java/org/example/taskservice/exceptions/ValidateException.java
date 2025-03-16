package org.example.taskservice.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ValidateException extends RuntimeException {
  private List<String> errors = new ArrayList<String>();

  public ValidateException(List<String> errors) {
    this.errors = errors;
  }

  public ValidateException(String message, List<String> errors) {
    super(message);
    this.errors = errors;
  }

  public ValidateException(String message, Throwable cause, List<String> errors) {
    super(message, cause);
    this.errors = errors;
  }

  public ValidateException(Throwable cause, List<String> errors) {
    super(cause);
    this.errors = errors;
  }

  public ValidateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, List<String> errors) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.errors = errors;
  }
}
