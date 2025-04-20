package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.controller.MessageController;
import com.village.bellevue.entity.MessageEntity;

@Component
public class MessageModelAssembler implements RepresentationModelAssembler<MessageEntity, EntityModel<MessageEntity>> {
  @Override
  public EntityModel<MessageEntity> toModel(MessageEntity message) {
    if (!getAuthenticatedUserId().equals(message.getSender().getUser()))
      return EntityModel.of(
        message,
        linkTo(methodOn(MessageController.class).markAsRead(message.getSender().getUser(), message.getId())).withRel("mark"),
        linkTo(methodOn(MessageController.class).message(message.getSender().getUser(), null)).withRel("message")
      );
    return EntityModel.of(
      message,
      linkTo(methodOn(MessageController.class).message(message.getReceiver().getUser(), null)).withRel("message")
    );
  }
}

