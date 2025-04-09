package com.village.bellevue.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.village.bellevue.entity.RatingEntity.Star;
import com.village.bellevue.service.PostService;
import com.village.bellevue.service.RatingService;

class RatingControllerTest {

  @Autowired private MockMvc mockMvc;
  @Mock private RatingService ratingService;
  @Mock private PostService postService;
  @InjectMocks private RatingController ratingController;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(ratingController).build();
  }

  @Test
  void testRatingSuccess() throws Exception {
    when(ratingService.rate(2l, Star.FIVE)).thenReturn(true);

    mockMvc
        .perform(put("/api/rating/2/five"))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  void testRatingFailure() throws Exception {
    when(postService.canRead(any())).thenReturn(false);

    mockMvc
        .perform(put("/api/rating/2/five"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }
}
