package com.village.bellevue.service;

import org.springframework.data.domain.Page;

public interface ForumTagService {
  public Page<String> searchTags(String query, int page, int size);
}
