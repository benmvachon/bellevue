package com.village.bellevue.repository;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.id.AggregateRatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AggregateRatingRepository
    extends JpaRepository<AggregateRatingEntity, AggregateRatingId> {}
