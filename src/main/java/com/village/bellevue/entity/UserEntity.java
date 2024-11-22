package com.village.bellevue.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "user")
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private UserStatus status = UserStatus.OFFLINE;

  @Column(nullable = false)
  private AvatarType avatar = AvatarType.CAT;

  private Timestamp created = new Timestamp(System.currentTimeMillis());
  private Timestamp updated = new Timestamp(System.currentTimeMillis());

  public enum UserStatus {
    OFFLINE,
    ONLINE,
    COOKING;

    @JsonValue
    public String toValue() {
      return this.name().toLowerCase();
    }

    @JsonCreator
    public static UserStatus fromString(String value) {
      return UserStatus.valueOf(value.toUpperCase());
    }
  }

  public enum AvatarType {
    CAT,
    RAPTOR,
    WALRUS,
    BEE,
    MONKEY,
    HORSE;

    @JsonValue
    public String toValue() {
      return this.name().toLowerCase();
    }

    @JsonCreator
    public static AvatarType fromString(String value) {
      return AvatarType.valueOf(value.toUpperCase());
    }
  }
}
