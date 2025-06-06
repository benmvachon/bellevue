package com.village.bellevue.error;

public class RatingException extends Exception {

  public RatingException() {
    super("An error occurred related to a rating.");
  }

  public RatingException(String message) {
    super(message);
  }

  public RatingException(Throwable cause) {
    super(cause);
  }

  public RatingException(String message, Throwable cause) {
    super(message, cause);
  }
}
