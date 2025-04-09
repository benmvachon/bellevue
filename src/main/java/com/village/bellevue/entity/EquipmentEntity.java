package com.village.bellevue.entity;

import java.sql.Timestamp;

import com.village.bellevue.entity.id.EquipmentId;

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
@Table(name = "equipment")
@IdClass(EquipmentId.class)
public class EquipmentEntity {
  @Id
  private Long user;

  @Id
  private String item;

  private String slot;
  private boolean equipped;
  private Timestamp unlocked = new Timestamp(System.currentTimeMillis());
}
