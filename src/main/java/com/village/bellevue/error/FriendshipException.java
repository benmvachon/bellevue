package com.village.bellevue.error;

public class FriendshipException extends Exception {

  public FriendshipException() {
    super("An error occurred related to a friend relationship.");
  }

  public FriendshipException(String message) {
    super(message);
  }

  public FriendshipException(Throwable cause) {
    super(cause);
  }

  public FriendshipException(String message, Throwable cause) {
    super(message, cause);
  }
}
