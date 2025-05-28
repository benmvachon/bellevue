package com.village.bellevue.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.assembler.ForumModelAssembler;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.service.ForumService;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

  private final ForumService forumService;
  private final ForumModelAssembler forumModelAssembler;
  private final PagedResourcesAssembler<ForumModel> pagedForumAssembler;

  public ForumController(
    ForumService forumService,
    ForumModelAssembler forumModelAssembler,
    PagedResourcesAssembler<ForumModel> pagedForumAssembler
  ) {
    this.forumService = forumService;
    this.forumModelAssembler = forumModelAssembler;
    this.pagedForumAssembler = pagedForumAssembler;
  }

  @PostMapping
  public ResponseEntity<EntityModel<ForumModel>> create(@RequestBody ForumEntity forum) {
    try {
      ForumModel createdForum = forumService.create(forum);
      EntityModel<ForumModel> entityModel = forumModelAssembler.toModel(createdForum);
      return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<ForumModel>>> readAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "9") int size
  ) {
    Page<ForumModel> forums = forumService.readAll(page, size);
    PagedModel<EntityModel<ForumModel>> pagedModel = pagedForumAssembler.toModel(forums, forumModelAssembler);
    return ResponseEntity.status(HttpStatus.OK).body(pagedModel);
  }

  @GetMapping("/unread")
  public ResponseEntity<PagedModel<EntityModel<ForumModel>>> readUnread(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "9") int size
  ) {
    Page<ForumModel> forums = forumService.readAllWithUnreadPosts(page, size);
    PagedModel<EntityModel<ForumModel>> pagedModel = pagedForumAssembler.toModel(forums, forumModelAssembler);
    return ResponseEntity.status(HttpStatus.OK).body(pagedModel);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<ForumModel>> read(@PathVariable Long id) {
    try {
      Optional<ForumModel> forum = forumService.read(id);
      return forum
        .map(forumEntity -> ResponseEntity.ok(forumModelAssembler.toModel(forumEntity)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PutMapping("/{forum}/notify")
  public ResponseEntity<Void> turnOnNotifications(@PathVariable Long forum) {
    try {
      if (forumService.turnOnNotifications(forum)) return ResponseEntity.status(HttpStatus.OK).build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PutMapping("/{forum}/mute")
  public ResponseEntity<Void> turnOffNotifications(@PathVariable Long forum) {
    try {
      if (forumService.turnOffNotifications(forum)) return ResponseEntity.status(HttpStatus.OK).build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    try {
      if (forumService.delete(id)) return ResponseEntity.status(HttpStatus.OK).build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }
}
