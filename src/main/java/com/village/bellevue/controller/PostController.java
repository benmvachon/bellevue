package com.village.bellevue.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.hateoas.EntityModel;
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

  public PostController(
    PostService postService,
    PostModelAssembler postModelAssembler
  ) {
    this.postService = postService;
    this.postModelAssembler = postModelAssembler;
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
  public ResponseEntity<List<PostModel>> readAllByForum(
    @PathVariable Long forum,
    @RequestParam(required = false) Long cursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(cursor)) cursor = System.currentTimeMillis();
      List<PostModel> posts = postService.readAllByForum(forum, new Timestamp(cursor), limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/forum/{forum}/count")
  public ResponseEntity<Long> countAllByForum(@PathVariable Long forum) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(postService.countAllByForum(forum));
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}")
  public ResponseEntity<List<PostModel>> readAllByParent(
    @PathVariable Long parent,
    @RequestParam(required = false) Long cursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(cursor)) cursor = System.currentTimeMillis();
      List<PostModel> posts = postService.readAllByParent(parent, new Timestamp(cursor), limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}/count")
  public ResponseEntity<Long> countAllByParent(@PathVariable Long parent) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(postService.countAllByParent(parent));
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}/{child}")
  public ResponseEntity<List<PostModel>> readOthersByParent(
    @PathVariable Long parent,
    @PathVariable Long child,
    @RequestParam(required = false) Long cursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(cursor)) cursor = System.currentTimeMillis();
      List<PostModel> posts = postService.readOthersByParent(parent, child, new Timestamp(cursor), limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}/{child}/count")
  public ResponseEntity<Long> countOthersByParent(@PathVariable Long parent, @PathVariable Long child) {
    try {
      return ResponseEntity.status(HttpStatus.OK).body(postService.countOthersByParent(parent, child));
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
