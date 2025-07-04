package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.village.bellevue.controller.PostController;
import com.village.bellevue.model.PostModel;

@Component
public class PostModelAssembler implements RepresentationModelAssembler<PostModel, EntityModel<PostModel>> {
  @Override
  public EntityModel<PostModel> toModel(PostModel post) {
    return EntityModel.of(
      post,
      linkTo(methodOn(PostController.class).readAllByParent(post.getId(), null, null, 1l)).withRel("children"),
      linkTo(methodOn(PostController.class).reply(post.getForum().getId(), post.getId(), null)).withRel("reply")
    );
  }
}

