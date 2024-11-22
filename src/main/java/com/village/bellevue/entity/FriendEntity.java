package com.village.bellevue.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.village.bellevue.entity.id.FriendId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "friend")
@IdClass(FriendId.class)
public class FriendEntity {

  @Id private Long user;

  @Id
  @ManyToOne
  @JoinColumn(name = "friend", nullable = false)
  private ScrubbedUserEntity friend;

  @Column(nullable = false)
  private FriendshipStatus status = FriendshipStatus.PENDING_THEM;

  private Timestamp created = new Timestamp(System.currentTimeMillis());
  private Timestamp updated = new Timestamp(System.currentTimeMillis());

  public enum FriendshipStatus {
    PENDING_THEM,
    PENDING_YOU,
    ACCEPTED,
    BLOCKED_THEM,
    BLOCKED_YOU;

    @JsonValue
    public String toValue() {
      return this.name().toLowerCase();
    }

    @JsonCreator
    public static FriendshipStatus fromString(String value) {
      return FriendshipStatus.valueOf(value.toUpperCase());
    }
  }
}
