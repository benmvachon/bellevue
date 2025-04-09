package com.village.bellevue.service;

import com.village.bellevue.entity.RatingEntity.Star;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.RatingException;

public interface RatingService {

  public boolean rate(Long post, Star rating) throws AuthorizationException, RatingException;
}
