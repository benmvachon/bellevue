package com.village.bellevue.event.equipment.post;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.village.bellevue.error.EquipmentException;
import com.village.bellevue.event.PostEvent;
import com.village.bellevue.event.equipment.AbstractEquipmentListener;
import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.repository.ItemRepository;
import com.village.bellevue.repository.PostRepository;

public abstract class AbstractEquipmentPostEventListener extends AbstractEquipmentListener<PostEvent> {

  private final PostRepository postRepository;

  public AbstractEquipmentPostEventListener(
    PostRepository postRepository,
    EquipmentRepository equipmentRepository,
    ApplicationEventPublisher publisher,
    ItemRepository itemRepository
  ) {
    super(equipmentRepository, itemRepository, publisher);
    this.postRepository = postRepository;
  }

  protected abstract String getKeyword();
  protected abstract int getRequiredOccurances();

  @Override
  protected boolean isEventRelevant(PostEvent event) {
    return !Strings.isNullOrEmpty(getKeyword()) && event.getPost().getContent().contains(getKeyword());
  }

  @EventListener
  @Transactional("asyncTransactionManager")
  public void handleEvent(PostEvent event) throws EquipmentException {
    super.handleEvent(event);
  }

  @Override
  protected boolean shouldUnlock(PostEvent event) {
    if (getRequiredOccurances() == 1) return true;
    return Strings.isNullOrEmpty(getKeyword()) ?
      postRepository.countAllPostsByUser(getUser(event)) >= getRequiredOccurances() :
      postRepository.countAllPostsByUserContainingKeyword(getUser(event), getKeyword()) >= getRequiredOccurances();
  }
}
