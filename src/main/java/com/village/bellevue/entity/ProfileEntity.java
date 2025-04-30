package com.village.bellevue.entity;

import java.sql.Timestamp;
import java.util.Map;

import org.springframework.data.annotation.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.village.bellevue.converter.JsonToMapConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Immutable
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "profile")
public class ProfileEntity {

  @Id
  private Long user;

  @Column(nullable = false, unique = true)
  private String name;
  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private Status status = Status.OFFLINE;

  @ManyToOne
  @JoinColumn(name = "avatar")
  private AvatarEntity avatar;

  @Convert(converter = JsonToMapConverter.class)
  private Map<String, String> equipment;

  @ManyToOne
  @JoinColumn(name = "location")
  private ForumEntity location;

  private Timestamp lastSeen = new Timestamp(System.currentTimeMillis());
  private String blackboard;

  public ProfileEntity(Long id) {
    this.user = id;
  }

  public ProfileEntity(UserEntity user) {
    this.user = user.getId();
    this.name = user.getName();
    this.username = user.getUsername();
  }

  public enum Status {
    OFFLINE,
    ACTIVE,
    IDLE;

    @JsonValue
    public String toValue() {
      return this.name().toUpperCase();
    }

    @JsonCreator
    public static Status fromString(String value) {
      return Status.valueOf(value.toUpperCase());
    }
  }
}
