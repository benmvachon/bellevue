package com.village.bellevue.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.assembler.ProfileModelAssembler;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.service.FriendService;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

  private final FriendService friendService;
  private final ProfileModelAssembler profileModelAssembler;
  private final PagedResourcesAssembler<ProfileModel> pagedAssembler;

  public FriendController(
    FriendService friendService,
    ProfileModelAssembler profileModelAssembler,
    PagedResourcesAssembler<ProfileModel> pagedAssembler
  ) {
    this.friendService = friendService;
    this.profileModelAssembler = profileModelAssembler;
    this.pagedAssembler = pagedAssembler;
  }

  @PostMapping("/{user}/request")
  public ResponseEntity<String> request(@PathVariable Long user) {
    try {
      friendService.request(user);
      return ResponseEntity.status(HttpStatus.CREATED).body("Friend request sent.");
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<ProfileModel>>> read(
    @RequestParam(defaultValue = "") String query,
    @RequestParam(required = false) List<Long> excluded,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    try {
      if (Objects.nonNull(query) && query.trim().isEmpty()) query = null;
      Page<ProfileModel> friends = friendService.readAll(query, excluded, page, size);
      PagedModel<EntityModel<ProfileModel>> pagedModel = pagedAssembler.toModel(friends, profileModelAssembler);
      return ResponseEntity.ok(pagedModel);
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/suggestions")
  public ResponseEntity<PagedModel<EntityModel<ProfileModel>>> readSuggestions(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    try {
      Page<ProfileModel> friends = friendService.readSuggestions(page, size);
      PagedModel<EntityModel<ProfileModel>> pagedModel = pagedAssembler.toModel(friends, profileModelAssembler);
      return ResponseEntity.ok(pagedModel);
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/{user}/status")
  public ResponseEntity<String> readStatus(@PathVariable Long user) {
    if (getAuthenticatedUserId().equals(user)) return ResponseEntity.ok("YOU");
    try {
      Optional<String> friend = friendService.getStatus(user);
      if (friend.isEmpty()) {
        return ResponseEntity.ok("UNSET");
      }
      return friend
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.ok("UNSET"));
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/{user}/friends")
  public ResponseEntity<PagedModel<EntityModel<ProfileModel>>> read(
    @PathVariable Long user,
    @RequestParam(defaultValue = "") String query,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    try {
      if (Objects.nonNull(query) && query.trim().isEmpty()) query = null;
      Page<ProfileModel> friends = friendService.readAll(user, query, page, size);
      PagedModel<EntityModel<ProfileModel>> pagedModel = pagedAssembler.toModel(friends, profileModelAssembler);
      return ResponseEntity.ok(pagedModel);
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PostMapping("/{user}/accept")
  public ResponseEntity<String> accept(@PathVariable Long user) {
    try {
      friendService.accept(user);
      return ResponseEntity.ok("Friend request accepted.");
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @DeleteMapping("/{user}/remove")
  public ResponseEntity<String> remove(@PathVariable Long user) {
    try {
      friendService.remove(user);
      return ResponseEntity.ok("Friend removed.");
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
