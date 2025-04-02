package com.village.bellevue.service.impl;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.BlackBoardEntity;
import com.village.bellevue.repository.BlackBoardRepository;
import com.village.bellevue.service.BlackBoardService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BlackBoardServiceImpl implements BlackBoardService {

  private final BlackBoardRepository blackBoardRepository;

  public BlackBoardServiceImpl(BlackBoardRepository blackBoardRepository) {
    this.blackBoardRepository = blackBoardRepository;
  }

  @Override
  public Optional<BlackBoardEntity> read() {
    return blackBoardRepository.findById(getAuthenticatedUserId());
  }

  @Override
  public Optional<BlackBoardEntity> read(Long user) {
    return blackBoardRepository.findById(user);
  }

  @Override
  public Optional<BlackBoardEntity> update(String blackboard) {
    BlackBoardEntity entity = new BlackBoardEntity(getAuthenticatedUserId(), blackboard);
    blackBoardRepository.save(entity);
    return Optional.of(entity);
  }
}
