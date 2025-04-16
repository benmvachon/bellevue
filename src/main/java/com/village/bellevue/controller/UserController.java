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

import com.village.bellevue.config.security.UserDetailsServiceImpl;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.service.UserProfileService;


@RestController
@RequestMapping("/api/user")
public class UserController {

  private final UserDetailsServiceImpl userService;
  private final UserProfileService profileService;

  public UserController(
    UserDetailsServiceImpl userService,
    UserProfileService profileService
  ) {
    this.userService = userService;
    this.profileService = profileService;
  }

  @PostMapping("/signup")
  public ResponseEntity<UserProfileEntity> create(@RequestBody UserEntity user) {
    try {
      UserProfileEntity scrubbedUser = userService.create(user);
      return ResponseEntity.status(HttpStatus.CREATED).body(scrubbedUser);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/{user}")
  public ResponseEntity<UserProfileEntity> read(@PathVariable Long user) {
    try {
      Optional<UserProfileEntity> friend = profileService.read(user);
      return friend
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/search/{prefix}")
  public ResponseEntity<Page<UserProfileEntity>> search(
    @PathVariable String prefix,
    @RequestParam(name = "p", defaultValue = "0") int page,
    @RequestParam(name = "n", defaultValue = "5") int size
  ) {
    Page<UserProfileEntity> users = profileService.readByNamePrefix(prefix, page, size);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/friends/{location}")
  public ResponseEntity<Page<UserProfileEntity>> friends(
    @PathVariable Long location,
    @RequestParam(name = "p", defaultValue = "0") int page,
    @RequestParam(name = "n", defaultValue = "5") int size
  ) {
    Page<UserProfileEntity> users = profileService.readFriendsByLocation(location, page, size);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/nonfriends/{location}")
  public ResponseEntity<Page<UserProfileEntity>> nonFriends(
    @PathVariable Long location,
    @RequestParam(name = "p", defaultValue = "0") int page,
    @RequestParam(name = "n", defaultValue = "5") int size
  ) {
    Page<UserProfileEntity> users = profileService.readNonFriendsByLocation(location, page, size);
    return ResponseEntity.ok(users);
  }

  @DeleteMapping
  public ResponseEntity<Void> delete() {
    try {
      userService.delete();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    return ResponseEntity.noContent().build();
  }
}
