package com.village.bellevue.error;

public class ForumException extends Exception {

  public ForumException() {
    super("An error occurred related to a forum.");
  }

  public ForumException(String message) {
    super(message);
  }

  public ForumException(Throwable cause) {
    super(cause);
  }

  public ForumException(String message, Throwable cause) {
    super(message, cause);
  }
}
