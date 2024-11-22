package com.village.bellevue.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.village.bellevue.entity.SkillEntity;
import com.village.bellevue.repository.SkillRepository;
import com.village.bellevue.service.SkillService;

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
