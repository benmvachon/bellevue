package com.village.bellevue.entity.id;

import java.io.Serializable;
import java.util.Objects;

import com.village.bellevue.entity.ItemEntity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class EquipmentId implements Serializable {
  private Long user;
  private ItemEntity item;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EquipmentId)) return false;
    EquipmentId that = (EquipmentId) o;
    return Objects.equals(user, that.user) && Objects.equals(item.getId(), that.item.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, item.getId());
  }
}