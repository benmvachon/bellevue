package com.village.bellevue.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.entity.RatingEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.RatingException;
import com.village.bellevue.service.RatingService;

@RestController
@RequestMapping("/api/rating")
public class RatingController {

  private final RatingService ratingService;

  public RatingController(RatingService ratingService) {
    this.ratingService = ratingService;
  }

  @PutMapping("/{post}/{star}")
  public ResponseEntity<Void> rate(@PathVariable Long post, @PathVariable String star) {
    try {
      if (ratingService.rate(post, RatingEntity.Star.fromString(star)))
        return ResponseEntity.status(HttpStatus.OK).build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (RatingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
}
