package com.village.bellevue.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.village.bellevue.entity.id.RatingId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@IdClass(RatingId.class)
@Table(
  name = "rating",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user", "post"})
  }
)
public class RatingEntity {

  @Id
  private Long post;
  @Id
  private Long user;

  @Column(nullable = false)
  private Star rating = Star.FIVE;

  private Timestamp created = new Timestamp(System.currentTimeMillis());
  private Timestamp updated = new Timestamp(System.currentTimeMillis());

  public RatingEntity(Long post, Long user, Star rating) {
    this.post = post;
    this.user = user;
    this.rating = rating;
  }

  public enum Star {
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE;

    @JsonValue
    public String toValue() {
      return this.name().toUpperCase();
    }

    @JsonCreator
    public static Star fromString(String value) {
      return Star.valueOf(value.toUpperCase());
    }
  }
}
