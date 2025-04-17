package com.village.bellevue.integration;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.PostModel;
import com.village.bellevue.model.ProfileModel;

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
  @SuppressWarnings("")
  void createUser() {
    ParameterizedTypeReference<EntityModel<ProfileModel>> responseType = new ParameterizedTypeReference<>() {};
    ResponseEntity<EntityModel<ProfileModel>> response = restTemplate.exchange(
      "http://localhost:" + port + "/api/user/signup",
      HttpMethod.POST,
      new HttpEntity<>(newUser),
      responseType
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertNotNull(response.getBody());
    assertEquals(21L, response.getBody().getContent().getId());

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

  @SuppressWarnings("")
  @Test
  @Order(4)
  void addForums() throws JsonProcessingException {
    login(newUser.getUsername());

    // Retrieve and verify the created post
    ForumEntity createdForum = createForum(forum, false);
    assertThat(createdForum).isNotNull();
    assertThat(createdForum.getUser()).isEqualTo(newUser.getId());
    assertThat(createdForum.getCategory()).isEqualTo(forum.getCategory());
    assertThat(createdForum.getName()).isEqualTo(forum.getName());

    ForumEntity retrievedForum = readForum(createdForum.getId(), false);
    assertThat(retrievedForum).isNotNull();
    assertThat(retrievedForum.getUser()).isEqualTo(newUser.getId());
    assertThat(retrievedForum.getCategory()).isEqualTo(forum.getCategory());
    assertThat(retrievedForum.getName()).isEqualTo(forum.getName());

    logout();
  }

  @SuppressWarnings("")
  @Test
  @Order(5)
  void addPost() throws JsonProcessingException {
    login(newUser.getUsername());

    PagedModel<EntityModel<ForumEntity>> forums = readForums(false);
    ForumEntity firstForum = forums.getContent().stream()
      .map(EntityModel::getContent)
      .filter(Objects::nonNull)
      .findFirst()
      .orElse(null); // or throw an exception if you expect it to exist
    post.setForum(firstForum);

    // Retrieve and verify the created post
    PostModel createdPost = createPost(firstForum.getId(), "This is a post", false);
    assertThat(createdPost).isNotNull();
    assertThat(createdPost.getContent()).isEqualTo(post.getContent());
    assertThat(createdPost.getUser().getUser()).isEqualTo(newUser.getId());

    PostModel readPost = readPost(createdPost.getId(), false);
    assertThat(readPost).isNotNull();
    assertThat(readPost.getContent()).isEqualTo(post.getContent());
    assertThat(readPost.getUser().getUser()).isEqualTo(newUser.getId());

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

  @SuppressWarnings("")
  private PostModel createPost(Long forum, String content, boolean expectFailure) {
    ParameterizedTypeReference<EntityModel<PostModel>> responseType = new ParameterizedTypeReference<>() {};
    ResponseEntity<EntityModel<PostModel>> response = restTemplate.exchange(
      "http://localhost:" + port + "/api/post/" + forum,
      HttpMethod.POST,
      new HttpEntity<>(content),
      responseType
    );

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.CREATED);
      return null;
    }
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return response.getBody().getContent();
  }

  @SuppressWarnings("")
  private PostModel readPost(Long postId, boolean expectFailure) {
    ParameterizedTypeReference<EntityModel<PostModel>> responseType = new ParameterizedTypeReference<>() {};
    ResponseEntity<EntityModel<PostModel>> response = restTemplate.exchange(
      "http://localhost:" + port + "/api/post/" + postId,
      HttpMethod.GET,
      null,
      responseType
    );

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    }
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody().getContent();
  }

  @SuppressWarnings("")
  private ForumEntity createForum(ForumEntity forum, boolean expectFailure) {
    // post to the post controller to create the forum
    ParameterizedTypeReference<EntityModel<ForumEntity>> responseType = new ParameterizedTypeReference<>() {};
    ResponseEntity<EntityModel<ForumEntity>> response = restTemplate.exchange(
      "http://localhost:" + port + "/api/forum",
      HttpMethod.POST,
      new HttpEntity<>(forum),
      responseType
    );

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.CREATED);
      return null;
    }
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return response.getBody().getContent();
  }

  @SuppressWarnings("")
  private ForumEntity readForum(long forum, boolean expectFailure) {
    ParameterizedTypeReference<EntityModel<ForumEntity>> responseType = new ParameterizedTypeReference<>() {};
    ResponseEntity<EntityModel<ForumEntity>> response = restTemplate.exchange(
      "http://localhost:" + port + "/api/forum/" + forum,
      HttpMethod.GET,
      null,
      responseType
    );

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    }
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody().getContent();
  }

  private PagedModel<EntityModel<ForumEntity>> readForums(boolean expectFailure) {
    String url = "http://localhost:" + port + "/api/forum";

    ParameterizedTypeReference<PagedModel<EntityModel<ForumEntity>>> responseType =
        new ParameterizedTypeReference<>() {};

    ResponseEntity<PagedModel<EntityModel<ForumEntity>>> response =
        restTemplate.exchange(url, HttpMethod.GET, null, responseType);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    }
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }
}
