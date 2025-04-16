package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.NotificationTypeEntity;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationTypeEntity, Long> {}
