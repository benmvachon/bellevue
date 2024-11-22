package com.village.bellevue.service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.village.bellevue.config.security.SecurityConfig;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.RecipeEntity.RecipeCategory;
import com.village.bellevue.entity.ReviewEntity;
import com.village.bellevue.entity.ReviewEntity.ReviewRating;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.repository.ReviewRepository;
import com.village.bellevue.service.impl.ReviewServiceImpl;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

  @InjectMocks private ReviewServiceImpl reviewService;
  @Mock private ReviewRepository reviewRepository;
  @Mock private RecipeService recipeService;
  @Mock private FriendService friendService;
  @Mock public RedisTemplate<String, Object> redisTemplate;

  private final ScrubbedUserEntity currentUser =
      new ScrubbedUserEntity(1L, "Foo", "foo", UserStatus.ONLINE, AvatarType.BEE);
  private final ScrubbedUserEntity friend =
      new ScrubbedUserEntity(2L, "Bar", "bar", UserStatus.ONLINE, AvatarType.WALRUS);
  private final RecipeEntity recipe =
      new RecipeEntity(
          3L,
          currentUser,
          "Recipe",
          "This is a recipe",
          RecipeCategory.MAIN,
          true,
          false,
          false,
          true,
          "nut",
          new Timestamp(System.currentTimeMillis()),
          new Timestamp(System.currentTimeMillis()),
          new HashSet<>(),
          new HashSet<>(),
          new HashSet<>(),
          new HashSet<>());
  private final ReviewEntity review =
      new ReviewEntity(
          4L,
          new SimpleRecipeEntity(recipe),
          friend,
          ReviewRating.ADMIRABLY,
          "Great!",
          new Timestamp(System.currentTimeMillis()),
          new Timestamp(System.currentTimeMillis()));

  @Test
  public void testReadAllByRecipe() throws Exception {
    when(recipeService.canRead(any())).thenReturn(true);
    Page<ReviewEntity> expectedPage = new PageImpl<>(List.of(review));
    when(reviewRepository.findByRecipeId(any(), eq(getAuthenticatedUserId()), any()))
        .thenReturn(expectedPage);

    Page<ReviewEntity> result = reviewService.readAllByRecipe(1L, 0, 10);

    assertEquals(expectedPage, result);
  }

  @Test
  public void testReadAllByRecipeUnauthorized() {
    when(recipeService.canRead(any())).thenReturn(false);
    try {
      throw assertThrows(
          AuthorizationException.class, () -> reviewService.readAllByRecipe(1L, 0, 10));
    } catch (AuthorizationException e) {
      // Do nothing this is expected
    }
  }

  @Test
  public void testReadAllByAuthor() throws Exception {
    when(friendService.isFriend(currentUser.getId())).thenReturn(true);
    Page<ReviewEntity> expectedPage = new PageImpl<>(List.of(review));
    when(reviewRepository.findByAuthorId(any(), any(), any())).thenReturn(expectedPage);
    try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
      mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(friend.getId());
      Page<ReviewEntity> result = reviewService.readAllByAuthor(1L, 0, 10);

      assertEquals(expectedPage, result);
    }
  }

  @Test
  public void testReadAllByAuthorUnauthorized() throws FriendshipException {
    when(friendService.isFriend(currentUser.getId())).thenReturn(false);
    try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
      mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(friend.getId());
      throw assertThrows(
          AuthorizationException.class, () -> reviewService.readAllByAuthor(1L, 0, 10));
    } catch (AuthorizationException e) {
      // Do nothing this is expected
    }
  }

  @Test
  public void testRead() throws Exception {
    when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
    when(reviewService.canRead(review.getId())).thenReturn(true);

    Optional<ReviewEntity> result = reviewService.read(review.getId());

    assertTrue(result.isPresent());
    assertEquals(review, result.get());
  }

  @Test
  public void testReadUnauthorized() {
    when(reviewService.canRead(review.getId())).thenReturn(false);
    try {
      throw assertThrows(AuthorizationException.class, () -> reviewService.read(review.getId()));
    } catch (AuthorizationException e) {
      // Do nothing this is expected
    }
  }

  @Test
  public void testUpdateUnauthorized() {
    when(reviewService.canUpdate(review.getId())).thenReturn(false);
    try {
      throw assertThrows(
          AuthorizationException.class, () -> reviewService.update(review.getId(), review));
    } catch (AuthorizationException e) {
      // Do nothing this is expected
    }
  }

  @Test
  public void testDeleteUnauthorized() {
    when(reviewService.canUpdate(review.getId())).thenReturn(false);
    try {
      throw assertThrows(AuthorizationException.class, () -> reviewService.delete(review.getId()));
    } catch (AuthorizationException e) {
      // Do nothing this is expected
    }
  }
}
