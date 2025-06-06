package com.village.bellevue.event.type;

import com.village.bellevue.entity.ProfileEntity.LocationType;

import lombok.Data;

@Data
public class LocationEvent implements UserEvent {
  private final Long user;
  private final Long location;
  private final LocationType locationType;
  private final boolean entrance;
}
