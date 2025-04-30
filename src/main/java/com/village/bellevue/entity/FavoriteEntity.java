package com.village.bellevue.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import com.village.bellevue.entity.id.FavoriteId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "favorite")
@IdClass(FavoriteId.class)
public class FavoriteEntity {

  @Id
  private Long user;

  @Id
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private FavoriteType type;

  @Id
  @Column(nullable = false)
  private Long entity;

  private Timestamp created = new Timestamp(System.currentTimeMillis());

  public enum FavoriteType {
    POST,
    FORUM,
    PROFILE;

    @JsonValue
    public String toValue() {
      return this.name().toUpperCase();
    }

    @JsonCreator
    public static FavoriteType fromString(String value) {
      return FavoriteType.valueOf(value.toUpperCase());
    }

    public boolean equals(String value) {
      return this.equals(fromString(value));
    }
  }
}
