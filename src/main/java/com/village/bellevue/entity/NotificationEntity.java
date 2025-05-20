package com.village.bellevue.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "notification")
public class NotificationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "notifier")
  private UserProfileEntity notifier;

  @Column(nullable = false)
  private Long notified;

  @Column(nullable = false)
  private NotificationType type = NotificationType.OTHER;

  private Long entity;

  @Column(name = "`read`", nullable = false)
  private boolean read = false;

  private Timestamp created = new Timestamp(System.currentTimeMillis());

  public enum NotificationType {
    FORUM,
    POST,
    REPLY,
    RATING,
    REQUEST,
    ACCEPTANCE,
    MESSAGE,
    EQUIPMENT,
    OTHER;

    @JsonValue
    public String toValue() {
      return this.name().toUpperCase();
    }

    @JsonCreator
    public static NotificationType fromString(String value) {
      return NotificationType.valueOf(value.toUpperCase());
    }

    public boolean equals(String value) {
      return this.equals(fromString(value));
    }
  }
}
