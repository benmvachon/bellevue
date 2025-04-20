package com.village.bellevue.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.assembler.NotificationModelAssembler;
import com.village.bellevue.entity.NotificationEntity;
import com.village.bellevue.service.NotificationService;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationModelAssembler notificationModelAssembler;
  private final PagedResourcesAssembler<NotificationEntity> pagedAssembler;

  public NotificationController(
    NotificationService notificationService,
    NotificationModelAssembler notificationModelAssembler,
    PagedResourcesAssembler<NotificationEntity> pagedAssembler
  ) {
    this.notificationService = notificationService;
    this.notificationModelAssembler = notificationModelAssembler;
    this.pagedAssembler = pagedAssembler;
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<NotificationEntity>>> readAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<NotificationEntity> notifications = notificationService.readAll(page, size);
    PagedModel<EntityModel<NotificationEntity>> pagedModel = pagedAssembler.toModel(notifications, notificationModelAssembler);
    return ResponseEntity.status(HttpStatus.OK).body(pagedModel);
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
