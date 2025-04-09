package com.village.bellevue.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.model.PostModel;
import com.village.bellevue.service.PostService;
import com.village.bellevue.service.RatingService;

@RestController
@RequestMapping("/api/post")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService, RatingService ratingService) {
    this.postService = postService;
  }

  @PostMapping
  public ResponseEntity<PostModel> create(@RequestBody PostEntity post) {
    try {
      PostModel createdPost = postService.create(post);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/forum/{forum}")
  public ResponseEntity<Page<PostModel>> readAllByForum(
      @PathVariable Long forum,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      Page<PostModel> posts = postService.readAllByForum(forum, page, size);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/children/{parent}")
  public ResponseEntity<Page<PostModel>> readAllByParent(
      @PathVariable Long parent,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      Page<PostModel> posts = postService.readAllByParent(parent, page, size);
      return ResponseEntity.status(HttpStatus.OK).body(posts);
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
