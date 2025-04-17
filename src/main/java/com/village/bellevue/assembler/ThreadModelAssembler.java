package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.village.bellevue.controller.MessageController;
import com.village.bellevue.entity.UserProfileEntity;

@Component
public class ThreadModelAssembler implements RepresentationModelAssembler<UserProfileEntity, EntityModel<UserProfileEntity>> {
  @Override
  public EntityModel<UserProfileEntity> toModel(UserProfileEntity thread) {
    return EntityModel.of(
      thread,
      linkTo(methodOn(MessageController.class).readAll(thread.getUser(), 0, 10)).withRel("read"),
      linkTo(methodOn(MessageController.class).markAsRead(thread.getUser())).withRel("mark")
    );
  }
}

