package com.village.bellevue.repository;

import com.village.bellevue.entity.SkillEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<SkillEntity, Long> {

  List<SkillEntity> findByNameStartingWithIgnoreCase(String prefix);
}
