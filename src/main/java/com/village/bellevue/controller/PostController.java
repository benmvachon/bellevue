package com.village.bellevue.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.assembler.PostModelAssembler;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.model.PostModel;
import com.village.bellevue.service.PostService;

@RestController
@RequestMapping("/api/post")
public class PostController {

  private final PostService postService;
  private final PostModelAssembler postModelAssembler;
  private final PagedResourcesAssembler<PostModel> pagedAssembler;

  public PostController(
    PostService postService,
    PostModelAssembler postModelAssembler,
    PagedResourcesAssembler<PostModel> pagedAssembler
  ) {
    this.postService = postService;
    this.postModelAssembler = postModelAssembler;
    this.pagedAssembler = pagedAssembler;
  }

  @PostMapping("/{forum}")
  public ResponseEntity<EntityModel<PostModel>> post(
    @PathVariable Long forum,
    @RequestBody String content
  ) {
    try {
      PostModel createdPost = postService.post(forum, content);
      EntityModel<PostModel> entityModel = postModelAssembler.toModel(createdPost);
      return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }


  @PostMapping("/{forum}/{parent}")
  public ResponseEntity<EntityModel<PostModel>> reply(
    @PathVariable Long forum,
    @PathVariable Long parent,
    @RequestBody String content
  ) {
    try {
      PostModel createdPost = postService.reply(forum, parent, content);
      EntityModel<PostModel> entityModel = postModelAssembler.toModel(createdPost);
      return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/forum/{forum}")
  public ResponseEntity<PagedModel<EntityModel<PostModel>>> readAllByForum(
    @PathVariable Long forum,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "true") boolean sortByRelevance
  ) {
    try {
      Page<PostModel> posts = postService.readAllByForum(forum, page, size, sortByRelevance);
      PagedModel<EntityModel<PostModel>> pagedModel = pagedAssembler.toModel(posts, postModelAssembler);
      return ResponseEntity.status(HttpStatus.OK).body(pagedModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}")
  public ResponseEntity<PagedModel<EntityModel<PostModel>>> readAllByParent(
    @PathVariable Long parent,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "true") boolean sortByRelevance
  ) {
    try {
      Page<PostModel> posts = postService.readAllByParent(parent, page, size, sortByRelevance);
      PagedModel<EntityModel<PostModel>> pagedModel = pagedAssembler.toModel(posts, postModelAssembler);
      return ResponseEntity.status(HttpStatus.OK).body(pagedModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}/{child}")
  public ResponseEntity<PagedModel<EntityModel<PostModel>>> readOthersByParent(
    @PathVariable Long parent,
    @PathVariable Long child,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "true") boolean sortByRelevance
  ) {
    try {
      Page<PostModel> posts = postService.readOthersByParent(parent, child, page, size, sortByRelevance);
      PagedModel<EntityModel<PostModel>> pagedModel = pagedAssembler.toModel(posts, postModelAssembler);
      return ResponseEntity.status(HttpStatus.OK).body(pagedModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<PostModel> read(@PathVariable Long id) {
    try {
      Optional<PostModel> post = postService.read(id);
      return post
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }
}
