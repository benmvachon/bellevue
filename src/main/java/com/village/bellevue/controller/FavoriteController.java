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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.assembler.FavoriteModelAssembler;
import com.village.bellevue.entity.FavoriteEntity;
import com.village.bellevue.entity.FavoriteEntity.FavoriteType;
import com.village.bellevue.service.FavoriteService;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final FavoriteModelAssembler favoriteModelAssembler;
  private final PagedResourcesAssembler<FavoriteEntity> pagedFavoriteAssembler;

  public FavoriteController(
    FavoriteService favoriteService,
    FavoriteModelAssembler favoriteModelAssembler,
    PagedResourcesAssembler<FavoriteEntity> pagedFavoriteAssembler
  ) {
    this.favoriteService = favoriteService;
    this.favoriteModelAssembler = favoriteModelAssembler;
    this.pagedFavoriteAssembler = pagedFavoriteAssembler;
  }

  @PostMapping("/post")
  public ResponseEntity<EntityModel<FavoriteEntity>> favoritePost(@RequestBody Long post) {
    Optional<FavoriteEntity> favorite = favoriteService.favoritePost(post);
    return favorite
      .map(favoriteEntity -> ResponseEntity.ok(favoriteModelAssembler.toModel(favoriteEntity)))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PostMapping("/forum")
  public ResponseEntity<EntityModel<FavoriteEntity>> favoriteForum(@RequestBody Long forum) {
    Optional<FavoriteEntity> favorite = favoriteService.favoriteForum(forum);
    return favorite
      .map(favoriteEntity -> ResponseEntity.ok(favoriteModelAssembler.toModel(favoriteEntity)))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PostMapping("/profile")
  public ResponseEntity<EntityModel<FavoriteEntity>> favoriteProfile(@RequestBody Long user) {
    Optional<FavoriteEntity> favorite = favoriteService.favoriteProfile(user);
    return favorite
      .map(favoriteEntity -> ResponseEntity.ok(favoriteModelAssembler.toModel(favoriteEntity)))
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<FavoriteEntity>>> readAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) FavoriteType type
  ) {
    Page<FavoriteEntity> favorites;
    if (Objects.nonNull(type)) favorites = favoriteService.readAllOfType(type, page, size);
    else favorites = favoriteService.readAll(page, size);
    PagedModel<EntityModel<FavoriteEntity>> pagedModel = pagedFavoriteAssembler.toModel(favorites, favoriteModelAssembler);
    return ResponseEntity.status(HttpStatus.OK).body(pagedModel);
  }

  @DeleteMapping("/post/{post}")
  public ResponseEntity<Void> unfavoritePost(@PathVariable Long post) {
    favoriteService.unfavoritePost(post);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @DeleteMapping("/forum/{forum}")
  public ResponseEntity<Void> unfavoriteForum(@PathVariable Long forum) {
    favoriteService.unfavoriteForum(forum);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @DeleteMapping("/profile/{user}")
  public ResponseEntity<Void> unfavoriteProfile(@PathVariable Long user) {
    favoriteService.unfavoriteProfile(user);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
