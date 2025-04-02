package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.FavoriteEntity;
import com.village.bellevue.entity.id.FavoriteId;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, FavoriteId> {
  Page<FavoriteEntity> findAllByUser(Long user, Pageable page);
}
