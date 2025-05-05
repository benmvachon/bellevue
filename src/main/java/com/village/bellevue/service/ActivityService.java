package com.village.bellevue.service;

import java.sql.Timestamp;

public interface ActivityService {
  public void updateLastSeen(Long user);
  public Timestamp getLastSeen(Long user);
  public void markUsersIdle(Timestamp lastSeen);
  public void markUserOffline(Long user);
}
