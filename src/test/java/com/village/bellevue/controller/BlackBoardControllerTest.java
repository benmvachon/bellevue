package com.village.bellevue.controller;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.village.bellevue.entity.BlackBoardEntity;
import com.village.bellevue.service.BlackBoardService;

public class BlackBoardControllerTest {

  @Autowired private MockMvc mockMvc;
  @InjectMocks private BlackBoardController BlackBoardController;
  @Mock private BlackBoardService blackBoardService;

  private final BlackBoardEntity blackBoard1 = new BlackBoardEntity(1L, "Welcome to my kitchen!");

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(BlackBoardController).build();
  }

  @Test
  public void testReadBlackBoardSuccess() throws Exception {
    when(blackBoardService.read()).thenReturn(Optional.of(blackBoard1));

    mockMvc
        .perform(get("/api/blackboard").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(blackBoard1.getBlackboard()));
    
    verify(blackBoardService).read();
  }

  @Test
  public void testReadBlackBoardByUserSuccess() throws Exception {
    when(blackBoardService.read(anyLong())).thenReturn(Optional.of(blackBoard1));

    mockMvc
        .perform(get("/api/blackboard/" + 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(blackBoard1.getBlackboard()));

    verify(blackBoardService).read(1L);
  }
}
