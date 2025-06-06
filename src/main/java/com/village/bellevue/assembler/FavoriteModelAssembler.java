package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.village.bellevue.controller.FavoriteController;
import com.village.bellevue.entity.FavoriteEntity;

@Component
public class FavoriteModelAssembler implements RepresentationModelAssembler<FavoriteEntity, EntityModel<FavoriteEntity>> {
  @Override
  public EntityModel<FavoriteEntity> toModel(FavoriteEntity favorite) {
    Link link;
    switch (favorite.getType()) {
      case FORUM:
        link = linkTo(methodOn(FavoriteController.class).unfavoriteForum(favorite.getEntity())).withRel("unfavorite");
        break;
      case POST:
        link = linkTo(methodOn(FavoriteController.class).unfavoritePost(favorite.getEntity())).withRel("unfavorite");
        break;
      case PROFILE:
      default:
        link = linkTo(methodOn(FavoriteController.class).unfavoriteProfile(favorite.getEntity())).withRel("unfavorite");
        break;
    }
    return EntityModel.of(favorite, link);
  }
}

