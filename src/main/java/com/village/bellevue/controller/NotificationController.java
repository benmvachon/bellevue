package com.village.bellevue.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.entity.NotificationEntity;
import com.village.bellevue.service.NotificationService;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public ResponseEntity<List<NotificationEntity>> readAll(
    @RequestParam(required = false) Long cursor,
    @RequestParam(defaultValue = "1") Long limit
  ) {
    if (Objects.isNull(cursor)) cursor = System.currentTimeMillis();
    List<NotificationEntity> notifications = notificationService.readAll(new Timestamp(cursor), limit);
    return ResponseEntity.status(HttpStatus.OK).body(notifications);
  }

  @GetMapping("/refresh")
  public ResponseEntity<List<NotificationEntity>> refresh(
    @RequestParam(required = false) Long cursor
  ) {
    if (Objects.isNull(cursor)) cursor = System.currentTimeMillis();
    List<NotificationEntity> notifications = notificationService.refresh(new Timestamp(cursor));
    return ResponseEntity.status(HttpStatus.OK).body(notifications);
  }

  @GetMapping("/{notification}")
  public ResponseEntity<NotificationEntity> read(@PathVariable Long notification) {
    return ResponseEntity.status(HttpStatus.OK).body(notificationService.read(notification));
  }

  @GetMapping("/total")
  public ResponseEntity<Long> total() {
    return ResponseEntity.status(HttpStatus.OK).body(notificationService.countTotal());
  }

  @GetMapping("/unread")
  public ResponseEntity<Long> unread() {
    return ResponseEntity.status(HttpStatus.OK).body(notificationService.countUnread());
  }

  @PutMapping("/read")
  public ResponseEntity<Void> markAsRead() {
    notificationService.markAllAsRead();
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping("/read/{notification}")
  public ResponseEntity<Void> markAsRead(@PathVariable Long notification) {
    notificationService.markAsRead(notification);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
