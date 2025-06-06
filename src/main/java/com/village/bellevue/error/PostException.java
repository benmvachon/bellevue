package com.village.bellevue.error;

public class PostException extends Exception {

  public PostException() {
    super("An error occurred related to a post.");
  }

  public PostException(String message) {
    super(message);
  }

  public PostException(Throwable cause) {
    super(cause);
  }

  public PostException(String message, Throwable cause) {
    super(message, cause);
  }
}
