package com.village.bellevue.repository;

import com.village.bellevue.entity.BlackBoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackBoardRepository extends JpaRepository<BlackBoardEntity, Long> {}
