package com.village.bellevue.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.village.bellevue.controller.ForumController;
import com.village.bellevue.model.CategoryModel;

@Component
public class CategoryModelAssembler implements RepresentationModelAssembler<String, EntityModel<CategoryModel>> {
  @Override
  public EntityModel<CategoryModel> toModel(String category) {
    CategoryModel model = new CategoryModel(category);
    return EntityModel.of(
      model,
      linkTo(methodOn(ForumController.class).readAllByCategory(model.getName(), 0, 10)).withRel("forums")
    );
  }
}

