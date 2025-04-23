package com.village.bellevue.event.equipment.rating;

import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;

import com.village.bellevue.entity.RatingEntity.Star;
import com.village.bellevue.event.RatingEvent;
import com.village.bellevue.event.equipment.AbstractEquipmentListener;
import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.repository.ItemRepository;
import com.village.bellevue.repository.RatingRepository;

public abstract class AbstractEquipmentRatingEventListener extends AbstractEquipmentListener<RatingEvent> {

  private final RatingRepository ratingRepository;

  public AbstractEquipmentRatingEventListener(
    RatingRepository ratingRepository,
    EquipmentRepository equipmentRepository,
    ApplicationEventPublisher publisher,
    ItemRepository itemRepository
  ) {
    super(equipmentRepository, itemRepository, publisher);
    this.ratingRepository = ratingRepository;
  }

  protected abstract Star getRating();
  protected abstract int getRequiredOccurances();
  protected abstract int getRatingCountThresholdForPost();
  protected abstract boolean isForRater();

  @Override
  protected boolean isEventRelevant(RatingEvent event) {
    return Objects.isNull(getRating()) || getRating().equals(event.getRating());
  }

  @Override
  protected Long getUser(RatingEvent event) {
    if (isForRater()) return event.getUser();
    return event.getPostAuthor();
  }

  @Override
  protected boolean shouldUnlock(RatingEvent event) {
    if (isForRater()) {
      if (getRequiredOccurances() == 1) return true;
      if (Objects.isNull(getRating())) return ratingRepository.countByUser(getUser(event)) >= getRequiredOccurances();
      return ratingRepository.countByUserAndRating(getRating(), getUser(event)) >= getRequiredOccurances();
    }
    if (Objects.isNull(getRating())) return ratingRepository.countByPost(event.getPost(), getUser(event)) >= getRatingCountThresholdForPost();
    return ratingRepository.countByRatingAndPost(event.getPost(), event.getRating(), getUser(event)) >= getRatingCountThresholdForPost();
  }
}
