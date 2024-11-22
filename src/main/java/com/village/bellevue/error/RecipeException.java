package com.village.bellevue.error;

public class RecipeException extends Exception {

  public RecipeException() {
    super("An error occurred related to a recipe.");
  }

  public RecipeException(String message) {
    super(message);
  }

  public RecipeException(Throwable cause) {
    super(cause);
  }

  public RecipeException(String message, Throwable cause) {
    super(message, cause);
  }
}
