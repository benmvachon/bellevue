package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.id.AggregateRatingId;

@Repository
public interface AggregateRatingRepository extends JpaRepository<AggregateRatingEntity, AggregateRatingId> {
}
