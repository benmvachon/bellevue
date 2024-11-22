package com.village.bellevue.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.village.bellevue.config.security.UserDetailsServiceImpl;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import java.sql.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Mock UserDetailsServiceImpl userService;
  @InjectMocks private UserController userController;

  private final UserEntity user =
      new UserEntity(
          1L,
          "Foo",
          "foo",
          "foo",
          UserStatus.ONLINE,
          AvatarType.BEE,
          new Timestamp(System.currentTimeMillis()),
          new Timestamp(System.currentTimeMillis()));

  private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  void testCreateUserSuccess() throws Exception {
    when(userService.create(user)).thenReturn(new ScrubbedUserEntity(user));

    mockMvc
        .perform(
            post("/api/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user)))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void testDeleteUserSuccess() throws Exception {
    doNothing().when(userService).delete();

    mockMvc.perform(delete("/api/user")).andExpect(MockMvcResultMatchers.status().isNoContent());
  }
}
