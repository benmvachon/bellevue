package com.village.bellevue.error;

public class EquipmentException extends Exception {

  public EquipmentException() {
    super("Equipment error");
  }

  public EquipmentException(String message) {
    super(message);
  }

  public EquipmentException(Throwable cause) {
    super(cause);
  }

  public EquipmentException(String message, Throwable cause) {
    super(message, cause);
  }
}
