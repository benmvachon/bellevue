package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.FavoriteEntity;
import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.entity.id.FavoriteId;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, FavoriteId> {
  Page<FavoriteEntity> findByUser(Long user, Pageable pageable);
  Page<FavoriteEntity> findByUserAndType(Long user, FavoriteType type, Pageable pageable);
}
