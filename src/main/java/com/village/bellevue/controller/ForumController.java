package com.village.bellevue.controller;

import java.util.Objects;
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
import com.village.bellevue.error.ForumException;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.service.ForumService;
import com.village.bellevue.service.ForumTagService;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

  private final ForumService forumService;
  private final ForumTagService tagService;
  private final ForumModelAssembler forumModelAssembler;
  private final PagedResourcesAssembler<ForumModel> pagedForumAssembler;

  public ForumController(
    ForumService forumService,
    ForumTagService tagService,
    ForumModelAssembler forumModelAssembler,
    PagedResourcesAssembler<ForumModel> pagedForumAssembler
  ) {
    this.forumService = forumService;
    this.tagService = tagService;
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
    } catch (ForumException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<ForumModel>>> readAll(
    @RequestParam(defaultValue = "") String query,
    @RequestParam(defaultValue = "false") boolean unread,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "9") int size
  ) {
    if (Objects.nonNull(query) && query.trim().isEmpty()) query = null;
    Page<ForumModel> forums = forumService.readAll(query, unread, page, size);
    PagedModel<EntityModel<ForumModel>> pagedModel = pagedForumAssembler.toModel(forums, forumModelAssembler);
    return ResponseEntity.status(HttpStatus.OK).body(pagedModel);
  }

  @GetMapping("/tags")
  public ResponseEntity<PagedModel<String>> findTags(
    @RequestParam(defaultValue = "") String query,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "9") int size
  ) {
    Page<String> tags = tagService.searchTags(query, page, size);
  
    PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
      tags.getSize(), tags.getNumber(), tags.getTotalElements(), tags.getTotalPages()
    );
  
    PagedModel<String> pagedModel = PagedModel.of(tags.getContent(), metadata);
    return ResponseEntity.ok(pagedModel);
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

  @PutMapping("/{id}")
  public ResponseEntity<EntityModel<ForumModel>> update(
    @PathVariable Long id,
    @RequestBody ForumEntity forum
  ) {
    try {
      ForumModel createdForum = forumService.update(id, forum);
      EntityModel<ForumModel> entityModel = forumModelAssembler.toModel(createdForum);
      return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ForumException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PutMapping("/{id}/removeSelf")
  public ResponseEntity<Void> removeSelf(@PathVariable Long id) {
    try {
      if (forumService.removeSelf(id)) return ResponseEntity.status(HttpStatus.OK).build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ForumException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
