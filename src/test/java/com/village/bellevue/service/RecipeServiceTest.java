package com.village.bellevue.service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import com.village.bellevue.config.security.SecurityConfig;
import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.RecipeEntity.RecipeCategory;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.entity.id.AggregateRatingId;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.error.RecipeException;
import com.village.bellevue.model.RecipeModel;
import com.village.bellevue.repository.AggregateRatingRepository;
import com.village.bellevue.repository.RecipeRepository;
import com.village.bellevue.service.impl.RecipeServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

    @InjectMocks
    private RecipeServiceImpl recipeService;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private AggregateRatingRepository ratingRepository;
    @Mock
    public RedisTemplate<String, Object> redisTemplate;

    private final ScrubbedUserEntity currentUser = new ScrubbedUserEntity(1L, "Foo", "foo", UserStatus.ONLINE, AvatarType.BEE);
    private final ScrubbedUserEntity friend = new ScrubbedUserEntity(2L, "Bar", "bar", UserStatus.ONLINE, AvatarType.WALRUS);
    private final RecipeEntity recipe = new RecipeEntity(
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
            new HashSet<>()
    );

    private final AggregateRatingId ratingId = new AggregateRatingId(2L, 3L);
    private final AggregateRatingEntity rating = new AggregateRatingEntity(2L, 3L, 4.5, 2, new Timestamp(System.currentTimeMillis()));

    @Test
    public void testCreateRecipe() throws AuthorizationException, RecipeException {
        when(recipeRepository.save(any(RecipeEntity.class))).thenReturn(recipe);
        when(recipeRepository.saveAndFlush(any(RecipeEntity.class))).thenReturn(recipe);

        RecipeModel createdRecipe = recipeService.create(recipe);

        assertNotNull(createdRecipe);
        assertEquals(recipe.getId(), createdRecipe.getId());
        verify(recipeRepository).save(recipe);
    }

    @Test
    public void testReadRecipeAuthorized() throws AuthorizationException, RecipeException, FriendshipException {
        when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));
        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));
        when(recipeRepository.canRead(recipe.getId(), friend.getId())).thenReturn(true);
        try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
            mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(friend.getId());
            Optional<RecipeModel> result = recipeService.read(recipe.getId());
            assertTrue(result.isPresent());
            assertEquals(recipe.getId(), result.get().getId());
        }
    }

    @Test
    public void testReadRecipeUnauthorized() throws FriendshipException {
        when(recipeRepository.canRead(recipe.getId(), friend.getId())).thenReturn(false);
        try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
            mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(friend.getId());
            throw assertThrows(AuthorizationException.class, () -> recipeService.read(recipe.getId()));
        } catch (AuthorizationException e) {
            // Do nothing, this is expected
        }
    }

    @Test
    public void testUpdateRecipeAuthorized() throws AuthorizationException, RecipeException, FriendshipException {
        when(recipeRepository.save(any(RecipeEntity.class))).thenReturn(recipe);
        when(recipeRepository.saveAndFlush(any(RecipeEntity.class))).thenReturn(recipe);
        when(recipeRepository.canUpdate(recipe.getId(), currentUser.getId())).thenReturn(true);
        try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
            mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(currentUser.getId());
            RecipeModel updatedRecipe = recipeService.update(recipe.getId(), recipe);
            assertNotNull(updatedRecipe);
            assertEquals(recipe.getId(), updatedRecipe.getId());
            verify(recipeRepository).save(recipe);
        }
    }

    @Test
    public void testUpdateRecipeUnauthorized() {
        when(recipeRepository.canUpdate(recipe.getId(), currentUser.getId())).thenReturn(false);
        try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
            mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(currentUser.getId());
            throw assertThrows(AuthorizationException.class, () -> recipeService.update(recipe.getId(), recipe));
        } catch (AuthorizationException e) {
            // Do nothing, this is expected
        }
    }

    @Test
    public void testDeleteRecipeAuthorized() throws AuthorizationException, RecipeException, FriendshipException {
        when(recipeRepository.canUpdate(recipe.getId(), currentUser.getId())).thenReturn(true);
        try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
            mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(currentUser.getId());
            recipeService.delete(recipe.getId());
            verify(recipeRepository).deleteById(recipe.getId());
        }
    }

    @Test
    public void testDeleteRecipeUnauthorized() throws FriendshipException {
        when(recipeRepository.canUpdate(recipe.getId(), currentUser.getId())).thenReturn(false);
        try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
            mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(currentUser.getId());
            throw assertThrows(AuthorizationException.class, () -> recipeService.delete(recipe.getId()));
        } catch (AuthorizationException e) {
            // Do nothing, this is expected
        }
    }
}
