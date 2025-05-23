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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.assembler.PostModelAssembler;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.PostException;
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
    } catch (PostException e) {
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
    } catch (PostException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/forum/{forum}")
  public ResponseEntity<List<PostModel>> readAllByForum(
    @PathVariable Long forum,
    @RequestParam(required = false) Long createdCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(createdCursor)) createdCursor = System.currentTimeMillis();
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readAllByForum(forum, new Timestamp(createdCursor), idCursor, limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/forum/{forum}/recent")
  public ResponseEntity<List<PostModel>> readRecentByForum(
    @PathVariable Long forum,
    @RequestParam(required = false) Long createdCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(createdCursor)) createdCursor = System.currentTimeMillis();
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readAllByForum(forum, new Timestamp(createdCursor), idCursor, limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/forum/{forum}/popular")
  public ResponseEntity<List<PostModel>> readPopularByForum(
    @PathVariable Long forum,
    @RequestParam(required = false) Long popularityCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(popularityCursor)) popularityCursor = Long.valueOf(Integer.MAX_VALUE);
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readAllByForum(forum, popularityCursor, idCursor, limit);
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
    @RequestParam(required = false) Long createdCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(createdCursor)) createdCursor = System.currentTimeMillis();
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readAllByParent(parent, new Timestamp(createdCursor), idCursor, limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}/recent")
  public ResponseEntity<List<PostModel>> readRecentByParent(
    @PathVariable Long parent,
    @RequestParam(required = false) Long createdCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(createdCursor)) createdCursor = System.currentTimeMillis();
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readAllByParent(parent, new Timestamp(createdCursor), idCursor, limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}/popular")
  public ResponseEntity<List<PostModel>> readPopularByParent(
    @PathVariable Long parent,
    @RequestParam(required = false) Long popularityCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(popularityCursor)) popularityCursor = Long.valueOf(Integer.MAX_VALUE);
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readAllByParent(parent, popularityCursor, idCursor, limit);
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
    @RequestParam(required = false) Long createdCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(createdCursor)) createdCursor = System.currentTimeMillis();
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readOthersByParent(parent, child, new Timestamp(createdCursor), idCursor, limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}/{child}/recent")
  public ResponseEntity<List<PostModel>> readRecentOthersByParent(
    @PathVariable Long parent,
    @PathVariable Long child,
    @RequestParam(required = false) Long createdCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(createdCursor)) createdCursor = System.currentTimeMillis();
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readOthersByParent(parent, child, new Timestamp(createdCursor), idCursor, limit);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}/{child}/popular")
  public ResponseEntity<List<PostModel>> readPopularOthersByParent(
    @PathVariable Long parent,
    @PathVariable Long child,
    @RequestParam(required = false) Long popularityCursor,
    @RequestParam(required = false) Long idCursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    try {
      if (Objects.isNull(popularityCursor)) popularityCursor = Long.valueOf(Integer.MAX_VALUE);
      if (Objects.isNull(idCursor)) idCursor = Long.valueOf(Integer.MAX_VALUE);
      List<PostModel> posts = postService.readOthersByParent(parent, child, popularityCursor, idCursor, limit);
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

  @PutMapping("/read/{post}")
  public ResponseEntity<Void> markAsRead(@PathVariable Long post) {
    try {
      postService.markAsRead(post);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping("/readall/{forum}")
  public ResponseEntity<Void> markForumAsRead(@PathVariable Long forum) {
    try {
      postService.markForumAsRead(forum);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping("/readall")
  public ResponseEntity<Void> markAllAsRead() {
    postService.markAllAsRead();
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
