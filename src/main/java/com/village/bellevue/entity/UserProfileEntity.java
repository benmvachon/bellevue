package com.village.bellevue.entity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.village.bellevue.converter.JsonToMapConverter;
import com.village.bellevue.entity.ProfileEntity.LocationType;
import com.village.bellevue.entity.ProfileEntity.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Immutable
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "user_profile")
public class UserProfileEntity {

  @Id
  private Long user;

  @Column(nullable = false, unique = true)
  private String name;
  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private Status status = Status.OFFLINE;

  @Column(nullable = false)
  private String avatar = "cat";

  @Convert(converter = JsonToMapConverter.class)
  private Map<String, String> equipment = new HashMap<>();

  @Column(nullable = true)
  private Long location;
  @Column(nullable = true)
  private LocationType locationType;

  private Timestamp lastSeen = new Timestamp(System.currentTimeMillis());
  private String blackboard = "";

  public UserProfileEntity(Long id) {
    this.user = id;
  }

  public UserProfileEntity(UserEntity user) {
    this.user = user.getId();
    this.name = user.getName();
    this.username = user.getUsername();
  }

  public UserProfileEntity(UserEntity user, String avatar) {
    this.user = user.getId();
    this.name = user.getName();
    this.username = user.getUsername();
    this.avatar = avatar;
  }
}
