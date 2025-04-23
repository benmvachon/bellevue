package com.village.bellevue.event.equipment.rating;

import org.springframework.stereotype.Component;

import com.village.bellevue.entity.RatingEntity.Star;
import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.repository.ItemRepository;
import com.village.bellevue.repository.RatingRepository;

@Component
public class SunGlassesUnlocker extends AbstractEquipmentRatingEventListener {

  public SunGlassesUnlocker(
    RatingRepository ratingRepository,
    EquipmentRepository equipmentRepository,
    ItemRepository itemRepository
  ) {
    super(ratingRepository, equipmentRepository, itemRepository);
  }

  @Override
  protected Star getRating() {
    return Star.FIVE;
  }

  @Override
  protected int getRequiredOccurances() {
    return 20;
  }

  @Override
  protected int getRatingCountThresholdForPost() {
    return 1;
  }

  @Override
  protected boolean isForRater() {
    return false;
  }

  @Override
  protected String getItemName() {
    return "sunglasses";
  }

  @Override
  protected String getItemSlot() {
    return "mask";
  }
  
}
