package com.village.bellevue.event;

import lombok.Data;

@Data
public class AcceptanceEvent {
  private final Long user;
  private final Long friend;
}
