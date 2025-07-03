package com.village.bellevue.assembler;

import java.util.Objects;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.controller.EquipmentController;
import com.village.bellevue.controller.FriendController;
import com.village.bellevue.controller.MessageController;
import com.village.bellevue.model.ProfileModel;

@Component
public class ProfileModelAssembler implements RepresentationModelAssembler<ProfileModel, EntityModel<ProfileModel>> {
  @Override
  public EntityModel<ProfileModel> toModel(ProfileModel profile) {
    if (!Objects.isNull(getAuthenticatedUserId()) && getAuthenticatedUserId().equals(profile.getId()))
      return EntityModel.of(
        profile,
        linkTo(methodOn(EquipmentController.class).readAll(0, 10, "all")).withRel("equipment")
      );
    String friendshipStatus = profile.getFriendshipStatus();
    if (Objects.isNull(friendshipStatus)) friendshipStatus = "UNSET";
    return switch (friendshipStatus.toUpperCase()) {
      case "ACCEPTED" -> EntityModel.of(
        profile,
        linkTo(methodOn(FriendController.class).remove(profile.getId())).withRel("remove"),
        linkTo(methodOn(FriendController.class).read(profile.getId(), "", 0, 10)).withRel("friends"),
        linkTo(methodOn(MessageController.class).message(profile.getId(), null)).withRel("message")
      );
      case "PENDING_YOU" -> EntityModel.of(
        profile,
        linkTo(methodOn(FriendController.class).accept(profile.getId())).withRel("accept")
      );
      case "PENDING_THEM" -> EntityModel.of(
        profile
      );
      default -> EntityModel.of(
        profile,
        linkTo(methodOn(FriendController.class).request(profile.getId())).withRel("request")
      );
    };
  }
}

