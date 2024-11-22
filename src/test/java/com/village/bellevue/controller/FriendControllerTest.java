package com.village.bellevue.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.FriendEntity.FriendshipStatus;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.service.FriendService;

public class FriendControllerTest {

  @Autowired private MockMvc mockMvc;
  @InjectMocks private FriendController friendController;
  @Mock private FriendService friendService;

  private final ScrubbedUserEntity currentUser =
      new ScrubbedUserEntity(1L, "Foo", "foo", UserStatus.ONLINE, AvatarType.BEE);
  private final ScrubbedUserEntity friend =
      new ScrubbedUserEntity(2L, "Bar", "bar", UserStatus.ONLINE, AvatarType.WALRUS);
  private final FriendEntity friendship =
      new FriendEntity(
          currentUser.getId(),
          friend,
          FriendshipStatus.ACCEPTED,
          new Timestamp(System.currentTimeMillis()),
          new Timestamp(System.currentTimeMillis()));

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(friendController).build();
  }

  @Test
  public void testRequestFriendSuccess() throws Exception {
    mockMvc
        .perform(post("/api/friend/1/request"))
        .andExpect(status().isCreated())
        .andExpect(content().string("Friend request sent."));

    verify(friendService).request(1L);
  }

  @Test
  public void testRequestFriendFailure() throws Exception {
    doThrow(new FriendshipException("User not found")).when(friendService).request(1L);

    mockMvc
        .perform(post("/api/friend/1/request"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User not found"));

    verify(friendService).request(1L);
  }

  @Test
  public void testReadFriendSuccess() throws Exception {
    ScrubbedUserEntity user = new ScrubbedUserEntity();
    when(friendService.read(1L)).thenReturn(Optional.of(user));

    mockMvc
        .perform(get("/api/friend/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(friendService).read(1L);
  }

  @Test
  public void testReadFriendNotFound() throws Exception {
    when(friendService.read(1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/friend/1")).andExpect(status().isNotFound());

    verify(friendService).read(1L);
  }

  @Test
  public void testReadFriendFailure() throws Exception {
    doThrow(new FriendshipException("User not found")).when(friendService).read(1L);

    mockMvc.perform(get("/api/friend/1")).andExpect(status().isBadRequest());

    verify(friendService).read(1L);
  }

  @Test
  public void testReadFriendsSuccess() throws Exception {
    Page<FriendEntity> friends = new PageImpl<>(List.of(friendship), PageRequest.of(0, 5), 1);
    when(friendService.readAll(1L, 0, 5)).thenReturn(friends);

    mockMvc
        .perform(get("/api/friend/1/friends?p=0&n=5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(friendService).readAll(1L, 0, 5);
  }

  @Test
  public void testReadFriendsFailure() throws Exception {
    doThrow(new FriendshipException("User not found")).when(friendService).readAll(1L, 0, 5);

    mockMvc.perform(get("/api/friend/1/friends?p=0&n=5")).andExpect(status().isBadRequest());

    verify(friendService).readAll(1L, 0, 5);
  }

  @Test
  public void testAcceptFriendSuccess() throws Exception {
    mockMvc
        .perform(post("/api/friend/1/accept"))
        .andExpect(status().isOk())
        .andExpect(content().string("Friend request accepted."));

    verify(friendService).accept(1L);
  }

  @Test
  public void testAcceptFriendFailure() throws Exception {
    doThrow(new FriendshipException("User not found")).when(friendService).accept(1L);

    mockMvc
        .perform(post("/api/friend/1/accept"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User not found"));

    verify(friendService).accept(1L);
  }

  @Test
  public void testBlockUserSuccess() throws Exception {
    mockMvc
        .perform(post("/api/friend/1/block"))
        .andExpect(status().isOk())
        .andExpect(content().string("User blocked."));

    verify(friendService).block(1L);
  }

  @Test
  public void testBlockUserFailure() throws Exception {
    doThrow(new FriendshipException("User not found")).when(friendService).block(1L);

    mockMvc
        .perform(post("/api/friend/1/block"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User not found"));

    verify(friendService).block(1L);
  }

  @Test
  public void testRemoveFriendSuccess() throws Exception {
    mockMvc
        .perform(delete("/api/friend/1/remove"))
        .andExpect(status().isOk())
        .andExpect(content().string("Friend removed."));

    verify(friendService).remove(1L);
  }

  @Test
  public void testRemoveFriendFailure() throws Exception {
    doThrow(new FriendshipException("User not found")).when(friendService).remove(1L);

    mockMvc
        .perform(delete("/api/friend/1/remove"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User not found"));

    verify(friendService).remove(1L);
  }
}
