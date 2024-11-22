package com.village.bellevue.error;

public class ReviewException extends Exception {

  public ReviewException() {
    super("An error occurred related to a review.");
  }

  public ReviewException(String message) {
    super(message);
  }

  public ReviewException(Throwable cause) {
    super(cause);
  }

  public ReviewException(String message, Throwable cause) {
    super(message, cause);
  }
}
