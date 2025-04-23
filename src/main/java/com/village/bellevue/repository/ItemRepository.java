package com.village.bellevue.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.ItemEntity;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
  Optional<ItemEntity> findByName(String name);
}
