package com.village.bellevue.service;

import java.util.List;

import com.village.bellevue.entity.SkillEntity;

public interface SkillService {

    public List<SkillEntity> search(String query);
}
