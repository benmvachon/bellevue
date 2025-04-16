package com.village.bellevue.controller;

import org.springframework.data.domain.Page;
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
import com.village.bellevue.service.RatingService;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService, RatingService ratingService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public ResponseEntity<Page<NotificationEntity>> readAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<NotificationEntity> notifications = notificationService.readAll(page, size);
    return ResponseEntity.status(HttpStatus.OK).body(notifications);
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
