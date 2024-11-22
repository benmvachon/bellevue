package com.village.bellevue.service;

import com.village.bellevue.entity.SkillEntity;
import java.util.List;

public interface SkillService {

  public List<SkillEntity> search(String query);
}
