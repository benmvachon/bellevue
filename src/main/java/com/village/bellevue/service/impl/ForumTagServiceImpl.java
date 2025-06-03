package com.village.bellevue.service.impl;

import static com.village.bellevue.config.security.SecurityConfig.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.village.bellevue.repository.ForumTagRepository;
import com.village.bellevue.service.ForumTagService;

@Service
public class ForumTagServiceImpl implements ForumTagService {

  private final ForumTagRepository repository;

  public ForumTagServiceImpl(ForumTagRepository repository) {
    this.repository = repository;
  }

  @Override
  public Page<String> searchTags(String query, int page, int size) {
    return repository.findByPrefix(getAuthenticatedUserId(), query, PageRequest.of(page, size));
  }
  
}
