package com.village.bellevue.entity;

import com.village.bellevue.entity.id.AggregateRatingId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "aggregate_rating")
@IdClass(AggregateRatingId.class)
public class AggregateRatingEntity {
  @Id private Long user;

  @Id private Long recipe;

  private Double rating;
  private Integer ratingCount;

  private Timestamp updated;
}
