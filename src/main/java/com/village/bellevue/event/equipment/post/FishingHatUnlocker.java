package com.village.bellevue.event.equipment.post;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.repository.ItemRepository;
import com.village.bellevue.repository.PostRepository;

@Component
public class FishingHatUnlocker extends AbstractEquipmentPostEventListener {

  public FishingHatUnlocker(
    PostRepository postRepository,
    EquipmentRepository equipmentRepository,
    ApplicationEventPublisher publisher,
    ItemRepository itemRepository
  ) {
    super(postRepository, equipmentRepository, publisher, itemRepository);
  }

  @Override
  protected String getKeyword() {
    return "trout";
  }

  @Override
  protected int getRequiredOccurances() {
    return 3;
  }

  @Override
  protected String getItemName() {
    return "fishing_hat";
  }

  @Override
  protected String getItemSlot() {
    return "hat";
  }

}
