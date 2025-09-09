package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.village.bellevue.entity.ForumSecurityEntity;
import com.village.bellevue.entity.id.ForumSecurityId;

public interface ForumSecurityRepository extends JpaRepository<ForumSecurityEntity, ForumSecurityId> {}
