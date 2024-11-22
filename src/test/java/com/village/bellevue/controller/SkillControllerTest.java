package com.village.bellevue.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.village.bellevue.entity.SkillEntity;
import com.village.bellevue.service.SkillService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SkillControllerTest {

  @Autowired private MockMvc mockMvc;
  @InjectMocks private SkillController SkillController;
  @Mock private SkillService SkillService;

  private final SkillEntity skill1 = new SkillEntity(1L, "Hammer");
  private final SkillEntity skill2 = new SkillEntity(2L, "Wrench");

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(SkillController).build();
  }

  @Test
  public void testSearchSkillSuccess() throws Exception {
    Gson gson = new Gson();

    List<SkillEntity> skillList = Arrays.asList(skill1, skill2);
    when(SkillService.search(anyString())).thenReturn(skillList);

    mockMvc
        .perform(get("/api/skill/search").param("query", "tool").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(gson.toJson(skillList)));
  }

  @Test
  public void testSearchSkillNotFound() throws Exception {
    when(SkillService.search(anyString())).thenReturn(Arrays.asList());

    mockMvc
        .perform(
            get("/api/skill/search").param("query", "unknown").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));
  }
}
