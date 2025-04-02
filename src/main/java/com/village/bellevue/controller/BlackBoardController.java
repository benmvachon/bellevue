package com.village.bellevue.controller;

import com.village.bellevue.entity.BlackBoardEntity;
import com.village.bellevue.service.BlackBoardService;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blackboard")
public class BlackBoardController {

  BlackBoardService blackBoardService;

  public BlackBoardController(BlackBoardService blackBoardService) {
    this.blackBoardService = blackBoardService;
  }

  @GetMapping
  public ResponseEntity<String> read() {
    Optional<BlackBoardEntity> entity = blackBoardService.read();
    return entity
        .map((b) -> ResponseEntity.ok(b.getBlackboard()))
        .orElseGet(() -> ResponseEntity.ok("Write a message..."));
  }

  @GetMapping("/{user}")
  public ResponseEntity<String> read(@PathVariable Long user) {
    Optional<BlackBoardEntity> entity = blackBoardService.read(user);
    return entity
        .map((b) -> ResponseEntity.ok(b.getBlackboard()))
        .orElseGet(() -> ResponseEntity.ok(""));
  }

  @PutMapping
  public ResponseEntity<String> update(@RequestBody String blackboard) {
    blackBoardService.update(blackboard);
    return ResponseEntity.ok(blackboard);
  }
}
