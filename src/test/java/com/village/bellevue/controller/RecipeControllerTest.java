package com.village.bellevue.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.RecipeEntity.RecipeCategory;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.RecipeException;
import com.village.bellevue.model.RecipeModel;
import com.village.bellevue.service.RecipeService;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RecipeControllerTest {

  @Autowired private MockMvc mockMvc;
  @Mock private RecipeService recipeService;
  @InjectMocks private RecipeController recipeController;

  private final ScrubbedUserEntity currentUser =
      new ScrubbedUserEntity(1L, "Foo", "foo", UserStatus.ONLINE, AvatarType.BEE);
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
  private final AggregateRatingEntity rating =
      new AggregateRatingEntity(2L, 3L, 4.5, 2, new Timestamp(System.currentTimeMillis()));
  private final RecipeModel model = new RecipeModel(recipe, Optional.of(rating));
  private final SimpleRecipeEntity simple = new SimpleRecipeEntity(recipe);

  private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(recipeController).build();
  }

  @Test
  void testCreateRecipeSuccess() throws Exception {
    when(recipeService.create(recipe)).thenReturn(model);

    mockMvc
        .perform(
            post("/api/recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(recipe)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService).create(recipe);
  }

  @Test
  void testReadRecipeSuccess() throws Exception {
    when(recipeService.read(recipe.getId())).thenReturn(Optional.of(model));

    mockMvc
        .perform(get("/api/recipe/" + recipe.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService).read(recipe.getId());
  }

  @Test
  void testReadRecipeNotFound() throws Exception {
    when(recipeService.read(recipe.getId())).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/recipe/" + recipe.getId())).andExpect(status().isNotFound());

    verify(recipeService).read(recipe.getId());
  }

  @Test
  void testReadRatingSuccess() throws Exception {
    when(recipeService.readRating(recipe.getId())).thenReturn(Optional.of(rating));

    mockMvc
        .perform(get("/api/recipe/" + recipe.getId() + "/rating"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService).readRating(recipe.getId());
  }

  @Test
  void testReadRatingNotFound() throws Exception {
    when(recipeService.readRating(recipe.getId())).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/recipe/" + recipe.getId() + "/rating"))
        .andExpect(status().isNotFound());

    verify(recipeService).readRating(recipe.getId());
  }

  @Test
  void testReadAllRecipesSuccess() throws Exception {
    Page<SimpleRecipeEntity> page = new PageImpl<>(Arrays.asList(simple), PageRequest.of(0, 5), 1);
    when(recipeService.readAll(currentUser.getId(), 0, 5)).thenReturn(page);

    mockMvc
        .perform(get("/api/recipe/author/" + currentUser.getId() + "?p=0&n=5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService).readAll(currentUser.getId(), 0, 5);
  }

  @Test
  void testUpdateRecipeSuccess() throws Exception {
    when(recipeService.update(recipe.getId(), recipe)).thenReturn(model);

    mockMvc
        .perform(
            put("/api/recipe/" + recipe.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(recipe)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService).update(recipe.getId(), recipe);
  }

  @Test
  void testDeleteRecipeSuccess() throws Exception {
    mockMvc.perform(delete("/api/recipe/" + recipe.getId())).andExpect(status().isNoContent());

    verify(recipeService).delete(recipe.getId());
  }

  @Test
  void testSearchRecipesSuccess() throws Exception {
    Page<SimpleRecipeEntity> page = new PageImpl<>(Arrays.asList(simple), PageRequest.of(0, 5), 1);
    when(recipeService.search("test", null, null, null, 0, 5)).thenReturn(page);

    mockMvc
        .perform(get("/api/recipe/search?q=test&p=0&n=5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(recipeService).search("test", null, null, null, 0, 5);
  }

  // Additional tests for exception handling
  @Test
  void testCreateRecipeAuthorizationException() throws Exception {
    when(recipeService.create(recipe)).thenThrow(new AuthorizationException("Forbidden"));

    mockMvc
        .perform(
            post("/api/recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(recipe)))
        .andExpect(status().isForbidden());
  }

  @Test
  void testReadRecipeAuthorizationException() throws Exception {
    when(recipeService.read(recipe.getId())).thenThrow(new AuthorizationException("Forbidden"));

    mockMvc.perform(get("/api/recipe/" + recipe.getId())).andExpect(status().isForbidden());
  }

  @Test
  void testReadRecipeException() throws Exception {
    when(recipeService.read(recipe.getId())).thenThrow(new RecipeException("Error"));

    mockMvc.perform(get("/api/recipe/" + recipe.getId())).andExpect(status().isBadRequest());
  }

  // Add similar tests for other methods to cover error cases
}
