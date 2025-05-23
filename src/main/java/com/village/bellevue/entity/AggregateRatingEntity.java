package com.village.bellevue.entity;

import java.sql.Timestamp;

import com.village.bellevue.entity.id.AggregateRatingId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
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

  @Id private Long post;

  private Double rating;
  private Integer ratingCount;
  private Integer popularity;

  private Timestamp updated;

  @Column(name = "`read`", nullable = false)
  private boolean read = false;
}
