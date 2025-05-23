package com.village.bellevue.entity;

import com.village.bellevue.entity.id.NotificationSettingId;

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
@IdClass(NotificationSettingId.class)
@Table(
  name = "notification_setting",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user", "forum"})
  }
)
public class NotificationSettingEntity {

  @Id
  private Long user;
  @Id
  private Long forum;

  @Column(nullable = false)
  private boolean notify = false;
}
