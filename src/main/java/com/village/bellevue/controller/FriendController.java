package com.village.bellevue.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.FriendEntity.FriendshipStatus;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.service.FriendService;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

  @Autowired private FriendService friendService;

  @PostMapping("/{user}/request")
  public ResponseEntity<String> request(@PathVariable Long user) {
    try {
      friendService.request(user);
      return ResponseEntity.status(HttpStatus.CREATED).body("Friend request sent.");
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @GetMapping("/{user}")
  public ResponseEntity<UserProfileEntity> read(@PathVariable Long user) {
    try {
      Optional<UserProfileEntity> friend = friendService.read(user);
      return friend
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/{user}/status")
  public ResponseEntity<String> readStatus(@PathVariable Long user) {
    if (getAuthenticatedUserId().equals(user)) return ResponseEntity.ok("YOU");
    try {
      Optional<String> friend = friendService.getStatus(user);
      if (friend.isEmpty() || FriendshipStatus.BLOCKED_YOU.equals(friend.get())) {
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
  public ResponseEntity<Page<FriendEntity>> read(
      @PathVariable Long user,
      @RequestParam(name = "p", defaultValue = "0") int page,
      @RequestParam(name = "n", defaultValue = "5") int size) {
    try {
      Page<FriendEntity> friends = friendService.readAll(user, page, size);
      return ResponseEntity.ok(friends);
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

  @PostMapping("/{user}/block")
  public ResponseEntity<String> block(@PathVariable Long user) {
    try {
      friendService.block(user);
      return ResponseEntity.ok("User blocked.");
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
