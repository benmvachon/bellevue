package com.village.bellevue.repository;

import com.village.bellevue.entity.ScrubbedUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrubbedUserRepository extends JpaRepository<ScrubbedUserEntity, Long> {

  ScrubbedUserEntity findByUsername(String username);
}
