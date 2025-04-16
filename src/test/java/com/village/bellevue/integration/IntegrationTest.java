package com.village.bellevue.integration;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.RatingEntity;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.PostModel;

@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest extends IntegrationTestWrapper {

  private static final Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  // this user will be added and then deleted
  private static final UserEntity newUser =
    new UserEntity(
      21L,
      "Foo",
      "foo",
      "foo",
      "foo@foo.foo",
      false,
      new Timestamp(System.currentTimeMillis()),
      new Timestamp(System.currentTimeMillis())
    );

  // this forum will be added and then deleted
  private final ForumEntity forum = new ForumEntity(
    3L,
    null,
    "Test Forum",
    "General",
    new Timestamp(System.currentTimeMillis())
  );
  // this forum will be added and then deleted
  private final PostEntity post = new PostEntity(
    3L,
    new UserProfileEntity(newUser),
    null,
    forum,
    "This is a post",
    new Timestamp(System.currentTimeMillis())
  );

  @Test
  @Order(1)
  @SuppressWarnings("null")
  void createUser() {
    ResponseEntity<UserProfileEntity> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/user/signup", newUser, UserProfileEntity.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertNotNull(response.getBody());
    assertEquals(21L, response.getBody().getUser());

    // make sure we can login as the new user
    login(newUser.getUsername());
    logout();
  }

  @Test
  @Order(2)
  void requestFriends() {
    login(newUser.getUsername());

    Long[] friendIds = {1L, 2L, 3L, 4L, 5L}; // bevis_armada, biggie, alice_j, mike_smith, emmy_b
    for (Long friendId : friendIds) {
      request(friendId, false);
    }

    block(6L, false); // liv_w
    logout();
  }

  @Test
  @Order(3)
  void acceptFriends() throws FriendshipException {
    acceptAs("bevis_armada", newUser.getId(), false); // id 1
    acceptAs("biggie", newUser.getId(), false); // id 2
    acceptAs("alice_j", newUser.getId(), false); // id 3
    acceptAs("mike_smith", newUser.getId(), false); // id 4

    acceptAs(
        "liam_wil",
        newUser.getId(),
        false); // id 9 was not requested, he should not be able to accept
    login("liam_wil");
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/friend/" + newUser.getId() + "/status",
            String.class);
    assertThat(response.getStatusCode())
        .isEqualTo(
            HttpStatus
                .OK);
    assertThat("UNSET".equals(response.getBody()));
    // liam_wil
    logout();

    requestAs("noah_d", newUser.getId(), false); // id 7

    requestAs("liv_w", newUser.getId(), false); // id 6 was blocked
    login("liv_w");
    response =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/friend/" + newUser.getId() + "/status",
            String.class);
    assertThat(response.getStatusCode())
        .isEqualTo(
            HttpStatus
                .OK);
    assertThat("UNSET".equals(response.getBody()));
    // liv_w
    logout();

    blockAs("ava_m", newUser.getId(), false); // id 8
  }

  @SuppressWarnings("null")
  @Test
  @Order(4)
  void addForums() throws JsonProcessingException {
    login(newUser.getUsername());

    PagedModel<ForumEntity> forums = readForums(false);
    post.setForum(forums.getContent().get(0));

    // post to the post controller to create the post
    ResponseEntity<ForumEntity> createResponse =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/forum", forum, ForumEntity.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Retrieve and verify the created post
    ForumEntity createdForum = createResponse.getBody();
    assertThat(createdForum).isNotNull();
    assertThat(createdForum.getUser()).isEqualTo(newUser.getId());
    assertThat(createdForum.getCategory()).isEqualTo(forum.getCategory());
    assertThat(createdForum.getName()).isEqualTo(forum.getName());

    ResponseEntity<ForumEntity> getResponse =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/forum/" + createdForum.getId(), ForumEntity.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    ForumEntity retrievedForum = getResponse.getBody();
    assertThat(retrievedForum).isNotNull();
    assertThat(retrievedForum.getUser()).isEqualTo(newUser.getId());
    assertThat(retrievedForum.getCategory()).isEqualTo(forum.getCategory());
    assertThat(retrievedForum.getName()).isEqualTo(forum.getName());

    logout();
  }

  @SuppressWarnings("null")
  @Test
  @Order(5)
  void addPost() throws JsonProcessingException {
    login(newUser.getUsername());

    PagedModel<ForumEntity> forums = readForums(false);
    post.setForum(forums.getContent().get(0));

    // post to the post controller to create the post
    ResponseEntity<PostModel> createResponse =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/post", post, PostModel.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Retrieve and verify the created post
    PostModel createdPost = createResponse.getBody();
    assertThat(createdPost).isNotNull();
    assertThat(createdPost.getContent()).isEqualTo(post.getContent());
    assertThat(createdPost.getUser().getUser()).isEqualTo(newUser.getId());
    logout();
  }

  private synchronized void login(String user) {
    logger.info("logging in as: " + user);
    String loginUrl = "http://localhost:" + port + "/api/user/login";
    MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();
    loginParams.add("username", user);
    loginParams.add("password", user);

    ResponseEntity<String> loginResponse =
        restTemplate.postForEntity(loginUrl, loginParams, String.class);

    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    HttpHeaders headers = loginResponse.getHeaders();
    List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
    assertThat(cookies).isNotEmpty();
    if (cookies != null) {
      restTemplate
          .getRestTemplate()
          .getInterceptors()
          .add(
              (request, body, execution) -> {
                request.getHeaders().add(HttpHeaders.COOKIE, cookies.get(0));
                return execution.execute(request, body);
              });
    }
    try {
      // give the system a little time to update the authenticated user
      wait(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private synchronized void logout() {
    logger.info("logging out");

    // Assuming /logout is the default logout endpoint configured in your application
    String logoutUrl = "http://localhost:" + port + "/api/user/logout";

    // Send a POST request to the logout URL
    ResponseEntity<String> logoutResponse =
        restTemplate.postForEntity(logoutUrl, null, String.class);

    // Verify that the response status is OK, indicating a successful logout
    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Clear any session cookies from the restTemplate to avoid reusing the session in future
    // requests
    restTemplate
        .getRestTemplate()
        .getInterceptors()
        .removeIf(interceptor -> interceptor instanceof ClientHttpRequestInterceptor);

    try {
      // Give the system a little time to update the session status
      wait(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void request(Long friend, boolean expectFailure) {
    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/friend/" + friend + "/request", null, Void.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.CREATED);
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
  }

  private void requestAs(String user, Long friend, boolean expectFailure) {
    login(user);
    request(friend, expectFailure);
    logout();
  }

  private void block(Long user, boolean expectFailure) {
    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/friend/" + user + "/block", null, Void.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
  }

  private void blockAs(String user, Long friend, boolean expectFailure) {
    login(user);
    block(friend, expectFailure);
    logout();
  }

  private void accept(Long friend, boolean expectFailure) {
    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/friend/" + friend + "/accept", null, Void.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
  }

  private void acceptAs(String user, Long friend, boolean expectFailure) {
    login(user);
    accept(friend, expectFailure);
    logout();
  }

  private PostModel readPost(Long postId, boolean expectFailure) {
    ResponseEntity<PostModel> response =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/post/" + postId, PostModel.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      return response.getBody();
    }
  }

  private PagedModel<ForumEntity> readForums(boolean expectFailure) throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/forum";

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, null, String.class);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      // Deserialize JSON response to Page<ForumEntity>
      ObjectMapper mapper = forumMapperWithPageSupport();
      JavaType pageType =
          mapper.getTypeFactory().constructParametricType(PagedModel.class, ForumEntity.class);
      return mapper.readValue(response.getBody(), pageType);
    }
  }

  private void addRating(RatingEntity rating, boolean expectFailure) {
    ResponseEntity<RatingEntity> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/rating", rating, RatingEntity.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.CREATED);
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
  }

  private PagedModel<RatingEntity> readRatings(boolean expectFailure) throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/rating";

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, null, String.class);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      // Deserialize JSON response to Page<SimplePostEntity>
      ObjectMapper mapper = ratingMapperWithPageSupport();
      JavaType pageType =
          mapper.getTypeFactory().constructParametricType(PagedModel.class, RatingEntity.class);
      return mapper.readValue(response.getBody(), pageType);
    }
  }

  private PagedModel<RatingEntity> readRatingsByPost(Long postId, boolean expectFailure)
      throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/rating/post/" + postId;

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, null, String.class);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      // Deserialize JSON response to Page<SimplePostEntity>
      ObjectMapper mapper = ratingMapperWithPageSupport();
      JavaType pageType =
          mapper.getTypeFactory().constructParametricType(PagedModel.class, RatingEntity.class);
      return mapper.readValue(response.getBody(), pageType);
    }
  }

  private PagedModel<RatingEntity> readRatingsByFriend(Long friend, boolean expectFailure)
      throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/rating/friend/" + friend;

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, null, String.class);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      // Deserialize JSON response to Page<SimplePostEntity>
      ObjectMapper mapper = ratingMapperWithPageSupport();
      JavaType pageType =
          mapper.getTypeFactory().constructParametricType(PagedModel.class, RatingEntity.class);
      return mapper.readValue(response.getBody(), pageType);
    }
  }

  private RatingEntity readRating(Long ratingId, boolean expectFailure) {
    ResponseEntity<RatingEntity> response =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/rating/" + ratingId, RatingEntity.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      return response.getBody();
    }
  }
}
