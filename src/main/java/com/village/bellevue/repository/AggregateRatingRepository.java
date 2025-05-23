package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.id.AggregateRatingId;

@Repository
public interface AggregateRatingRepository extends JpaRepository<AggregateRatingEntity, AggregateRatingId> {

  @Modifying
  @Query("UPDATE AggregateRatingEntity a SET a.read = true WHERE a.user = :user AND a.post = :post AND a.read = false")
  @Transactional
  int markAsRead(@Param("user") Long user, @Param("post") Long post);

  @Modifying
  @Query(value =
    "UPDATE aggregate_rating a " +
    "LEFT JOIN post p ON p.id = a.post " +
    "SET a.read = true " +
    "WHERE a.user = :user AND p.forum = :forum AND a.read = false",
    nativeQuery = true
  )
  @Transactional
  int markForumAsRead(@Param("user") Long user, @Param("forum") Long forum);

  @Modifying
  @Query("UPDATE AggregateRatingEntity a SET a.read = true WHERE a.user = :user AND a.read = false")
  @Transactional
  int markAllAsRead(@Param("user") Long user);
}
