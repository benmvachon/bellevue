package com.village.bellevue.service.impl;

import com.village.bellevue.entity.SkillEntity;
import com.village.bellevue.repository.SkillRepository;
import com.village.bellevue.service.SkillService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SkillServiceImpl implements SkillService {

  private final SkillRepository skillRepository;

  public SkillServiceImpl(SkillRepository skillRepository) {
    this.skillRepository = skillRepository;
  }

  @Override
  public List<SkillEntity> search(String query) {
    return skillRepository.findByNameStartingWithIgnoreCase(query);
  }
}
