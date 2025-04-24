package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.village.bellevue.controller.PostController;
import com.village.bellevue.controller.UserController;
import com.village.bellevue.model.ForumModel;

@Component
public class ForumModelAssembler implements RepresentationModelAssembler<ForumModel, EntityModel<ForumModel>> {
  @Override
  public EntityModel<ForumModel> toModel(ForumModel forum) {
    if (forum.getUser() != null) {
      return EntityModel.of(
        forum,
        linkTo(methodOn(PostController.class).readAllByForum(forum.getId(), 0, 10, true)).withRel("posts"),
        linkTo(methodOn(PostController.class).post(forum.getId(), null)).withRel("post"),
        linkTo(methodOn(UserController.class).read(forum.getUser().getId())).withRel("user")
      );
    }
    return EntityModel.of(
      forum,
      linkTo(methodOn(PostController.class).readAllByForum(forum.getId(), 0, 10, true)).withRel("posts"),
      linkTo(methodOn(PostController.class).post(forum.getId(), null)).withRel("post")
    );
  }
}

