package com.village.bellevue.service;

import com.village.bellevue.entity.ReviewEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.ReviewException;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ReviewService {

  public Long create(ReviewEntity review) throws AuthorizationException, ReviewException;

  public Optional<ReviewEntity> read(Long id) throws AuthorizationException, ReviewException;

  public Page<ReviewEntity> readAll(int page, int size)
      throws AuthorizationException, ReviewException;

  public Page<ReviewEntity> readAllByRecipe(Long recipe, int page, int size)
      throws AuthorizationException, ReviewException;

  public Page<ReviewEntity> readAllByAuthor(Long author, int page, int size)
      throws AuthorizationException, ReviewException;

  public Page<ReviewEntity> readIncomplete(int page, int size)
      throws AuthorizationException, ReviewException;

  public ReviewEntity update(Long id, ReviewEntity updatedReview)
      throws AuthorizationException, ReviewException;

  public void delete(Long id) throws AuthorizationException, ReviewException;

  public boolean canRead(Long id);

  public boolean canUpdate(Long id);
}
