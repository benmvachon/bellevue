package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.ScrubbedUserEntity;

@Repository
public interface ScrubbedUserRepository extends JpaRepository<ScrubbedUserEntity, Long> {

    ScrubbedUserEntity findByUsername(String username);
}