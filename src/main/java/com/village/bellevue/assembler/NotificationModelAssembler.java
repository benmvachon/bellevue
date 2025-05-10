package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.village.bellevue.controller.ForumController;
import com.village.bellevue.controller.MessageController;
import com.village.bellevue.controller.NotificationController;
import com.village.bellevue.controller.PostController;
import com.village.bellevue.controller.UserController;
import com.village.bellevue.entity.NotificationEntity;

@Component
public class NotificationModelAssembler implements RepresentationModelAssembler<NotificationEntity, EntityModel<NotificationEntity>> {
  @Override
  public EntityModel<NotificationEntity> toModel(NotificationEntity notification) {
    Link entityLink = linkTo(methodOn(UserController.class).read(notification.getNotifier().getUser())).withRel("entity");
    if (notification.getType().getId().equals(1l)) {
      entityLink = linkTo(methodOn(ForumController.class).read(notification.getEntity())).withRel("entity");
    } else if (notification.getType().getId().equals(2l) || notification.getType().getId().equals(3l) || notification.getType().getId().equals(4l)) {
      entityLink = linkTo(methodOn(PostController.class).read(notification.getEntity())).withRel("entity");
    } else if (notification.getType().getId().equals(5l) || notification.getType().getId().equals(6l)) {
      entityLink = linkTo(methodOn(UserController.class).read(notification.getEntity())).withRel("entity");
    } else if (notification.getType().getId().equals(7l)) {
      entityLink = linkTo(methodOn(MessageController.class).readAll(notification.getEntity(), null, 10l)).withRel("entity");
    }
    
    return EntityModel.of(
      notification,
      linkTo(methodOn(NotificationController.class).markAsRead(notification.getId())).withRel("mark"),
      entityLink
    );
  }
}

