package com.village.bellevue.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

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

  @GetMapping("/{message}")
  public ResponseEntity<MessageEntity> read(@PathVariable Long message) {
    return ResponseEntity.status(HttpStatus.OK).body(messageService.read(message));
  }

  @GetMapping("/threads")
  public ResponseEntity<List<MessageEntity>> readThreads(
    @RequestParam(required = false) Long cursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    if (Objects.isNull(cursor)) cursor = System.currentTimeMillis();
    List<MessageEntity> threads = messageService.readThreads(new Timestamp(cursor), limit);
    return ResponseEntity.status(HttpStatus.OK).body(threads);
  }

  @GetMapping("/threads/refresh")
  public ResponseEntity<List<MessageEntity>> refreshThreads(
    @RequestParam(required = false) Long cursor
  ) {
    if (Objects.isNull(cursor)) cursor = System.currentTimeMillis();
    List<MessageEntity> threads = messageService.refreshThreads(new Timestamp(cursor));
    return ResponseEntity.status(HttpStatus.OK).body(threads);
  }

  @GetMapping("/total")
  public ResponseEntity<Long> countThreads() {
    return ResponseEntity.status(HttpStatus.OK).body(messageService.countThreads());
  }

  @GetMapping("/unread")
  public ResponseEntity<Long> readUnreadThreads() {
    return ResponseEntity.status(HttpStatus.OK).body(messageService.countUnreadThreads());
  }

  @GetMapping("/{friend}/all")
  public ResponseEntity<List<MessageEntity>> readAll(
    @PathVariable Long friend,
    @RequestParam(required = false) Long cursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    if (Objects.isNull(cursor)) cursor = System.currentTimeMillis();
    List<MessageEntity> messages = messageService.readAll(friend, new Timestamp(cursor), limit);
    return ResponseEntity.status(HttpStatus.OK).body(messages);
  }

  @GetMapping("/{friend}/total")
  public ResponseEntity<Long> countAll(@PathVariable Long friend) {
    return ResponseEntity.status(HttpStatus.OK).body(messageService.countAll(friend));
  }

  @PutMapping("/read")
  public ResponseEntity<Void> markAsRead() {
    messageService.markAllAsRead();
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping("/{friend}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable Long friend) {
    messageService.markThreadAsRead(friend);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping("/{friend}/read/{message}")
  public ResponseEntity<Void> markAsRead(
    @PathVariable Long friend,
    @PathVariable Long message
  ) {
    messageService.markAsRead(friend, message);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
