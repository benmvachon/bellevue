package com.village.bellevue.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.service.ForumService;
import com.village.bellevue.service.RatingService;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

  private final ForumService forumService;

  public ForumController(ForumService forumService, RatingService ratingService) {
    this.forumService = forumService;
  }

  @PostMapping
  public ResponseEntity<ForumEntity> create(@RequestBody ForumEntity forum) {
    ForumEntity createdForum = forumService.create(forum);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdForum);
  }

  @GetMapping
  public ResponseEntity<Page<ForumEntity>> readAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.OK).body(forumService.readAll(page, size));
  }

  @GetMapping("/category")
  public ResponseEntity<Page<String>> readAllCategories(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.OK).body(forumService.readAllCategories(page, size));
  }

  @GetMapping("/category/{category}")
  public ResponseEntity<Page<ForumEntity>> readAllByCategory(
      @PathVariable String category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.OK).body(forumService.readAllByCategory(category, page, size));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ForumEntity> read(@PathVariable Long id) {
    try {
      Optional<ForumEntity> forum = forumService.read(id);
      return forum
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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
