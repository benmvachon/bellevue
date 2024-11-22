package com.village.bellevue.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.RecipeEntity.RecipeCategory;
import com.village.bellevue.entity.ReviewEntity;
import com.village.bellevue.entity.ReviewEntity.ReviewRating;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.service.ReviewService;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReviewControllerTest {

  @Autowired private MockMvc mockMvc;
  @Mock private ReviewService reviewService;
  @InjectMocks private ReviewController reviewController;

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

  Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
  }

  @Test
  void testCreateReviewSuccess() throws Exception {
    when(reviewService.create(review)).thenReturn(review.getId());
    when(reviewService.read(review.getId())).thenReturn(Optional.of(review));

    mockMvc
        .perform(
            post("/api/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(review)))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void testReadReviewSuccess() throws Exception {
    when(reviewService.read(review.getId())).thenReturn(Optional.of(review));

    mockMvc
        .perform(get("/api/review/" + review.getId()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void testReadReviewNotFound() throws Exception {
    when(reviewService.read(review.getId())).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/review/" + review.getId()))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  void testReadAllByRecipeSuccess() throws Exception {
    Page<ReviewEntity> reviewPage = new PageImpl<>(List.of(review), PageRequest.of(0, 5), 1);
    when(reviewService.readAllByRecipe(recipe.getId(), 0, 5)).thenReturn(reviewPage);

    mockMvc
        .perform(get("/api/review/recipe/" + recipe.getId()).param("p", "0").param("n", "5"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void testUpdateReviewSuccess() throws Exception {
    when(reviewService.update(review.getId(), review)).thenReturn(review);

    mockMvc
        .perform(
            put("/api/review/" + review.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(review)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void testDeleteReviewSuccess() throws Exception {
    mockMvc
        .perform(delete("/api/review/" + review.getId()))
        .andExpect(MockMvcResultMatchers.status().isNoContent());
  }

  @Test
  void testReadAllByAuthorSuccess() throws Exception {
    Page<ReviewEntity> reviewPage = new PageImpl<>(List.of(review), PageRequest.of(0, 5), 1);
    when(reviewService.readAllByAuthor(review.getAuthor().getId(), 0, 5)).thenReturn(reviewPage);

    mockMvc
        .perform(get("/api/review/author/" + friend.getId()).param("p", "0").param("n", "5"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }
}
