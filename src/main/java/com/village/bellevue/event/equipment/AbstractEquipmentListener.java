package com.village.bellevue.event.equipment;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.entity.ItemEntity;
import com.village.bellevue.event.UserEvent;
import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.repository.ItemRepository;

import jakarta.transaction.Transactional;

public abstract class AbstractEquipmentListener<T extends UserEvent> {
  private final EquipmentRepository equipmentRepository;
  private final ItemRepository itemRepository;

  private Long item;

  public AbstractEquipmentListener(
    EquipmentRepository equipmentRepository,
    ItemRepository itemRepository
  ) {
    this.equipmentRepository = equipmentRepository;
    this.itemRepository = itemRepository;

    this.item = getItem();
  }

  protected abstract boolean isEventRelevant(T event);
  protected abstract boolean shouldUnlock(T event);
  protected abstract String getItemName();
  protected abstract String getItemSlot();

  protected Long getItem() {
    if (Objects.isNull(item)) {
      Optional<ItemEntity> optional = itemRepository.findByName(getItemName());
      if (optional.isPresent()) this.item = optional.get().getId();
      else this.item = itemRepository.save(new ItemEntity(null, getItemName(), getItemSlot(), false, true)).getId();
    }
    
    return item;
  }

  protected Long getUser(T event) {
    return event.getUser();
  }

  @Async
  @EventListener
  @Transactional
  public void handleEvent(T event) {
    if (isEventRelevant(event) && shouldUnlock(event)) unlock(event);
  }

  @Async
  @Transactional
  protected void unlock(T event) {
    Long user = getUser(event);
    ItemEntity item = itemRepository.getReferenceById(this.item);
    EquipmentEntity equipment = new EquipmentEntity(user, item, false, new Timestamp(System.currentTimeMillis()));
    equipmentRepository.save(equipment);
  }
}
