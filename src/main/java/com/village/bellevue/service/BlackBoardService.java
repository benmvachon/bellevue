package com.village.bellevue.service;

import com.village.bellevue.entity.BlackBoardEntity;
import java.util.Optional;

public interface BlackBoardService {
  Optional<BlackBoardEntity> read();

  Optional<BlackBoardEntity> read(Long user);

  Optional<BlackBoardEntity> update(String blackboard);
}
