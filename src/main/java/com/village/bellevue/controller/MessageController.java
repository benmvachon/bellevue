package com.village.bellevue.controller;

import org.springframework.data.domain.Page;
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

import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.service.MessageService;

@RestController
@RequestMapping("/api/message")
public class MessageController {

  private final MessageService messageService;

  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @PostMapping("/{friend}")
  public ResponseEntity<Void> message(
    @PathVariable Long friend,
    @RequestBody String message
  ) {
    try {
      messageService.message(friend, message);
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping
  public ResponseEntity<Page<UserProfileEntity>> readThreads(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Page<UserProfileEntity> threads = messageService.readThreads(page, size);
    return ResponseEntity.status(HttpStatus.OK).body(threads);
  }

  @GetMapping("/unread")
  public ResponseEntity<Page<UserProfileEntity>> readUnreadThreads(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Page<UserProfileEntity> threads = messageService.readUnreadThreads(page, size);
    return ResponseEntity.status(HttpStatus.OK).body(threads);
  }

  @GetMapping("/{friend}")
  public ResponseEntity<Page<MessageEntity>> readAll(
    @PathVariable Long friend,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    Page<MessageEntity> messages = messageService.readAll(friend, page, size);
    return ResponseEntity.status(HttpStatus.OK).body(messages);
  }

  @PutMapping("/{friend}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable Long friend) {
    messageService.markAllAsRead(friend);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping("/{friend}/read/{message}")
  public ResponseEntity<Void> markAsRead(
    @PathVariable Long friend,
    @PathVariable Long message
  ) {
    messageService.markAsRead(message);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
