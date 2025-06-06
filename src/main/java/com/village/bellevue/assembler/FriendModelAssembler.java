package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.village.bellevue.controller.FriendController;
import com.village.bellevue.controller.UserController;
import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.FriendEntity.FriendshipStatus;

@Component
public class FriendModelAssembler implements RepresentationModelAssembler<FriendEntity, EntityModel<FriendEntity>> {
  @Override
  public EntityModel<FriendEntity> toModel(FriendEntity friend) {
    if (FriendshipStatus.ACCEPTED.equals(friend.getStatus()))
      return EntityModel.of(
        friend,
        linkTo(methodOn(UserController.class).read(friend.getFriend().getUser())).withRel("user"),
        linkTo(methodOn(FriendController.class).read(friend.getFriend().getUser(), "", 0, 10)).withRel("friends")
      );
    return EntityModel.of(friend);
  }
}

