package com.village.bellevue.event.equipment;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.entity.ItemEntity;
import com.village.bellevue.entity.id.EquipmentId;
import com.village.bellevue.error.EquipmentException;
import com.village.bellevue.event.type.EquipmentEvent;
import com.village.bellevue.event.type.UserEvent;
import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.repository.ItemRepository;

public abstract class AbstractEquipmentListener<T extends UserEvent> {
  private final EquipmentRepository equipmentRepository;
  private final ItemRepository itemRepository;
  private final ApplicationEventPublisher publisher;

  private Long item;

  public AbstractEquipmentListener(
    EquipmentRepository equipmentRepository,
    ItemRepository itemRepository,
    ApplicationEventPublisher publisher
  ) {
    this.equipmentRepository = equipmentRepository;
    this.itemRepository = itemRepository;
    this.publisher = publisher;

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

  @Transactional(value = "asyncTransactionManager", timeout = 300)
  protected void handleEvent(T event) throws EquipmentException {
    if (!(event instanceof T)) return;
    if (equipmentRepository.existsById(new EquipmentId(getUser(event), itemRepository.getReferenceById(getItem())))) return;
    if (isEventRelevant(event) && shouldUnlock(event)) unlock(event);
  }

  @Modifying
  protected void unlock(T event) throws EquipmentException {
    Long user = getUser(event);
    ItemEntity item = itemRepository.findById(getItem()).orElseThrow(() -> new EquipmentException("Failed to find item to unlock for user"));
    EquipmentEntity equipment = new EquipmentEntity(user, item, false, new Timestamp(System.currentTimeMillis()));
    equipment = equipmentRepository.save(equipment);
    publisher.publishEvent(new EquipmentEvent(user, equipment));
  }
}
