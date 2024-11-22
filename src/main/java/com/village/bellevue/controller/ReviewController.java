package com.village.bellevue.controller;

import com.village.bellevue.entity.ReviewEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.ReviewException;
import com.village.bellevue.service.ReviewService;
import java.net.URI;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
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

@RestController
@RequestMapping("/api/review")
public class ReviewController {

  private final ReviewService reviewService;

  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @PostMapping
  public ResponseEntity<ReviewEntity> create(@RequestBody ReviewEntity review) {
    try {
      Optional<ReviewEntity> createdReview = reviewService.read(reviewService.create(review));
      return createdReview
          .map(
              entity ->
                  ResponseEntity.created(URI.create("/api/review/" + entity.getId())).body(entity))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ReviewException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReviewEntity> read(@PathVariable Long id) {
    try {
      Optional<ReviewEntity> review = reviewService.read(id);
      return review
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ReviewException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping()
  public ResponseEntity<PagedModel<ReviewEntity>> readAll(
      @RequestParam(name = "p", defaultValue = "0") int page,
      @RequestParam(name = "n", defaultValue = "5") int size) {
    try {
      Page<ReviewEntity> reviews = reviewService.readAll(page, size);
      return ResponseEntity.ok(new PagedModel<>(reviews));
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ReviewException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/recipe/{recipe}")
  public ResponseEntity<PagedModel<ReviewEntity>> readAllByRecipe(
      @PathVariable Long recipe,
      @RequestParam(name = "p", defaultValue = "0") int page,
      @RequestParam(name = "n", defaultValue = "5") int size) {
    try {
      Page<ReviewEntity> reviews = reviewService.readAllByRecipe(recipe, page, size);
      return ResponseEntity.ok(new PagedModel<>(reviews));
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ReviewException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/author/{author}")
  public ResponseEntity<PagedModel<ReviewEntity>> readAllByAuthor(
      @PathVariable Long author,
      @RequestParam(name = "p", defaultValue = "0") int page,
      @RequestParam(name = "n", defaultValue = "5") int size) {
    try {
      Page<ReviewEntity> reviews = reviewService.readAllByAuthor(author, page, size);
      return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(reviews));
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ReviewException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/incomplete")
  public ResponseEntity<PagedModel<ReviewEntity>> readIncomplete(
      @RequestParam(name = "p", defaultValue = "0") int page,
      @RequestParam(name = "n", defaultValue = "5") int size) {
    try {
      Page<ReviewEntity> reviews = reviewService.readIncomplete(page, size);
      return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(reviews));
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ReviewException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<ReviewEntity> update(
      @PathVariable Long id, @RequestBody ReviewEntity updatedReview) {
    try {
      ReviewEntity review = reviewService.update(id, updatedReview);
      return ResponseEntity.ok(review);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ReviewException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    try {
      reviewService.delete(id);
      return ResponseEntity.noContent().build();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (ReviewException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
}
