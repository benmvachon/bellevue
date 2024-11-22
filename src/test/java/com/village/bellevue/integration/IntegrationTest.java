package com.village.bellevue.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.entity.IngredientEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.RecipeEntity.RecipeCategory;
import com.village.bellevue.entity.RecipeEquipmentEntity;
import com.village.bellevue.entity.RecipeIngredientEntity;
import com.village.bellevue.entity.RecipeSkillEntity;
import com.village.bellevue.entity.RecipeStepEntity;
import com.village.bellevue.entity.ReviewEntity;
import com.village.bellevue.entity.ReviewEntity.ReviewRating;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;
import com.village.bellevue.entity.SkillEntity;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.IngredientModel;
import com.village.bellevue.model.RecipeModel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.village.bellevue.entity.FriendEntity.FriendshipStatus;

@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest extends IntegrationTestWrapper {

  private static final Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  // this user will be added and then deleted
  private static final UserEntity newUser =
      new UserEntity(21L, "Foo", "foo", "foo", UserStatus.ONLINE, AvatarType.BEE, null, null);

  // this recipe will be added and then deleted
  private static final RecipeEntity recipe =
      new RecipeEntity(
          null,
          null,
          "Spaghetti Bolognese",
          "A delicious and classic Italian pasta dish.",
          RecipeCategory.MAIN,
          false,
          false,
          false,
          false,
          null,
          null,
          null,
          new HashSet<>(),
          new HashSet<>(),
          new HashSet<>(),
          new HashSet<>());

  @Test
  @Order(1)
  @SuppressWarnings("null")
  void createUser() {
    ResponseEntity<ScrubbedUserEntity> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/user/signup", newUser, ScrubbedUserEntity.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertNotNull(response.getBody());
    assertEquals(21L, response.getBody().getId());

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
    ResponseEntity<FriendshipStatus> response =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/friend/" + newUser.getId() + "/status",
            FriendshipStatus.class);
    assertThat(response.getStatusCode())
        .isEqualTo(
            HttpStatus
                .NOT_FOUND); // even after "accepting" the new user, they should not be visible to
    // liam_wil
    logout();

    requestAs("noah_d", newUser.getId(), false); // id 7

    requestAs("liv_w", newUser.getId(), false); // id 6 was blocked
    login("liv_w");
    response =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/friend/" + newUser.getId() + "/status",
            FriendshipStatus.class);
    assertThat(response.getStatusCode())
        .isEqualTo(
            HttpStatus
                .NOT_FOUND); // even after "requesting" the new user, they should not be visible to
    // liv_w
    logout();

    blockAs("ava_m", newUser.getId(), false); // id 8
  }

  @SuppressWarnings("null")
  @Test
  @Order(4)
  void addRecipe() {
    login(newUser.getUsername());

    recipe.addStep(
        new RecipeStepEntity(
            null,
            1,
            "Heat olive oil in a large pan, add diced onions, carrots, and celery, and cook until softened."));
    recipe.addStep(
        new RecipeStepEntity(
            null, 2, "Add minced garlic and ground beef, and cook until the beef is browned."));
    recipe.addStep(
        new RecipeStepEntity(
            null,
            3,
            "Stir in tomato paste, canned tomatoes, and a splash of red wine, then season with salt, pepper, and Italian herbs."));
    recipe.addStep(
        new RecipeStepEntity(
            null,
            4,
            "Reduce the heat and let the sauce simmer for 20–30 minutes, stirring occasionally."));
    recipe.addStep(
        new RecipeStepEntity(
            null,
            5,
            "Serve the sauce over cooked spaghetti and top with freshly grated Parmesan."));

    recipe.addIngredient(getIngredient("tomato", 2.0, null));
    recipe.addIngredient(getIngredient("carrot", 1.0, null));
    recipe.addIngredient(getIngredient("onion", 1.0, null));
    recipe.addIngredient(getIngredient("garlic", 2.0, "cloves"));
    recipe.addIngredient(getIngredient("pasta", 2.0, "cups"));

    recipe.addSkill(getSkill("mince"));
    recipe.addSkill(getSkill("boil"));

    recipe.addEquipment(getEquipment("pot"));
    recipe.addEquipment(getEquipment("pan"));
    recipe.addEquipment(getEquipment("stove"));
    recipe.addEquipment(getEquipment("knife"));

    // post to the recipe controller to create the recipe
    ResponseEntity<RecipeModel> createResponse =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/recipe", recipe, RecipeModel.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // Retrieve and verify the created recipe
    RecipeModel createdRecipe = createResponse.getBody();
    assertThat(createdRecipe).isNotNull();
    assertThat(createdRecipe.getName()).isEqualTo("Spaghetti Bolognese");
    assertThat(createdRecipe.getAuthor().getId()).isEqualTo(newUser.getId());
    assertThat(createdRecipe.getSteps()).containsKeys(1, 2, 3, 4, 5);
    assertThat(createdRecipe.getSteps())
        .containsValue(
            "Heat olive oil in a large pan, add diced onions, carrots, and celery, and cook until softened.");
    assertThat(createdRecipe.getIngredients()).isNotEmpty();
    assertThat(createdRecipe.getIngredients())
        .containsAll(
            recipe.getIngredients().stream()
                .map(ingredient -> new IngredientModel(ingredient))
                .collect(Collectors.toList()));
    assertThat(createdRecipe.getSkills()).containsValues("mince", "boil");
    assertThat(createdRecipe.getEquipment()).containsValues("pot", "pan", "stove", "knife");

    recipe.setSteps(new HashSet<>());
    recipe.addStep(new RecipeStepEntity(null, 1, "Mince garlic."));
    recipe.addStep(new RecipeStepEntity(null, 2, "Dice tomatoes, carrots, and celery."));
    recipe.addStep(
        new RecipeStepEntity(
            null,
            3,
            "Heat olive oil in a large pan, add diced onions, carrots, and celery, and cook until softened."));
    recipe.addStep(
        new RecipeStepEntity(
            null, 4, "Add minced garlic and ground beef, and cook until the beef is browned."));
    recipe.addStep(
        new RecipeStepEntity(
            null,
            5,
            "Stir in tomato paste, canned tomatoes, and a splash of red wine, then season with salt, pepper, and Italian herbs."));
    recipe.addStep(
        new RecipeStepEntity(
            null,
            6,
            "Reduce the heat and let the sauce simmer for 20–30 minutes, stirring occasionally."));
    recipe.addStep(
        new RecipeStepEntity(
            null,
            7,
            "Serve the sauce over cooked spaghetti and top with freshly grated Parmesan."));

    recipe.addIngredient(getIngredient("celery", 1.0, "stalk"));

    ResponseEntity<RecipeModel> updateResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/recipe/" + createdRecipe.getId(),
            HttpMethod.PUT,
            new HttpEntity<>(recipe),
            RecipeModel.class);

    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    RecipeModel updatedRecipe = updateResponse.getBody();
    assertThat(updatedRecipe).isNotNull();
    assertThat(updatedRecipe.getName()).isEqualTo("Spaghetti Bolognese");
    assertThat(updatedRecipe.getAuthor().getId()).isEqualTo(newUser.getId());
    assertThat(updatedRecipe.getSteps()).containsKeys(1, 2, 3, 4, 5, 6, 7);
    assertThat(updatedRecipe.getSteps()).containsValue("Mince garlic.");
    assertThat(updatedRecipe.getIngredients()).isNotEmpty();
    assertThat(updatedRecipe.getIngredients())
        .containsAll(
            recipe.getIngredients().stream()
                .map(ingredient -> new IngredientModel(ingredient))
                .collect(Collectors.toList()));
    assertThat(updatedRecipe.getSkills()).containsValues("mince", "boil");
    assertThat(updatedRecipe.getEquipment()).containsValues("pot", "pan", "stove", "knife");
    logout();
  }

  @Test
  @Order(5)
  void readRecipes() throws JsonProcessingException {
    // login as the new user and attempt to read a recipe
    login(newUser.getUsername());

    RecipeModel successfulRead = readRecipe(1L, false);
    assertThat(successfulRead.getName()).isEqualTo("spicy chip-pea stew");
    readRecipe(3L, true);
    for (SimpleRecipeEntity searchedRecipe : searchRecipes("", false).getContent()) {
      assertThat(searchedRecipe.getAuthor().getId())
          .isIn(
              1L, 2L, 3L, 4L,
              21L); // the recipe's author must be one of the user's friends or the user themself
    }
    logout();

    // login as one of the new user's accepted friends
    login("bevis_armada");
    // read the new user's recipe
    successfulRead = readRecipe(5L, false);
    assertThat(successfulRead.getName()).isEqualTo(recipe.getName());
    assertThat(searchRecipes("", false).getContent())
        .contains(new SimpleRecipeEntity(successfulRead));
    logout();

    // login as one of the new user's pending friends
    login("emmy_b");
    // confirm that the new user's recipe cannot be read
    readRecipe(5L, true);
    // accept the friend request
    accept(newUser.getId(), false);
    // read the new user's recipe
    successfulRead = readRecipe(5L, false);
    assertThat(successfulRead.getName()).isEqualTo(recipe.getName());
    assertThat(searchRecipes("", false).getContent())
        .contains(new SimpleRecipeEntity(successfulRead));
    logout();

    // login as the user blocked by the new user
    login("liv_w");
    // confirm that the new user's recipe cannot be read
    readRecipe(5L, true);
    logout();
  }

  @Test
  @Order(6)
  void addReviews() throws JsonProcessingException {
    // login as the new user
    login(newUser.getUsername());
    // write reviews for accepted friends' recipes
    addReview(
        new ReviewEntity(
            null,
            new SimpleRecipeEntity(readRecipe(1L, false)),
            null, // the author should be applied automatically
            ReviewRating.ADMIRABLY,
            "I loved this!",
            null,
            null),
        false);
    logout();

    // attempt to write a review for a non-friend user's recipe and confirm it doesn't work
    login("ava_m");
    SimpleRecipeEntity privateRecipe = new SimpleRecipeEntity(readRecipe(3L, false));
    logout();
    login(newUser.getUsername());
    addReview(
        new ReviewEntity(
            null,
            privateRecipe,
            null, // the author should be applied automatically
            ReviewRating.DISASTROUSLY,
            "I hated this!",
            null,
            null),
        true);
    logout();

    // login as a friend of the new user
    login("bevis_armada");
    // write a review for the new user's recipe
    addReview(
        new ReviewEntity(
            null,
            new SimpleRecipeEntity(readRecipe(5L, false)),
            null, // the author should be applied automatically
            ReviewRating.SPLENDIDLY,
            "I liked this!",
            null,
            null),
        false);
    logout();

    // login as a friend of the new user
    login("biggie");
    // write a review for the new user's recipe
    addReview(
        new ReviewEntity(
            null,
            new SimpleRecipeEntity(readRecipe(5L, false)),
            null, // the author should be applied automatically
            ReviewRating.ADMIRABLY,
            "I'm okay with this!",
            null,
            null),
        false);
    logout();

    // login as a friend of the new user
    login("alice_j");
    // write a review for the new user's recipe
    addReview(
        new ReviewEntity(
            null,
            new SimpleRecipeEntity(readRecipe(5L, false)),
            null, // the author should be applied automatically
            ReviewRating.MUDDLINGLY,
            "I didn't like this!",
            null,
            null),
        false);
    logout();
  }

  @Test
  @Order(7)
  void updateReviews() throws JsonProcessingException {
    login(newUser.getUsername());
    ReviewEntity blankReview = startCooking(2L, false);
    assertThat(blankReview.getReview()).isNull();
    assertThat(blankReview.getContent()).isNull();
    assertThat(blankReview.getAuthor().getId()).isEqualTo(newUser.getId());

    blankReview.setReview(ReviewRating.ADMIRABLY);
    blankReview.setContent("I loved it!");
    blankReview.setUpdated(new Timestamp(System.currentTimeMillis()));

    ReviewEntity updatedReview = updateReview(blankReview, false);
    assertThat(updatedReview.getId()).isEqualTo(blankReview.getId());
    assertThat(updatedReview.getContent()).isEqualTo("I loved it!");
    assertThat(updatedReview.getReview()).isEqualTo(ReviewRating.ADMIRABLY);
    logout();
  }

  @Test
  @Order(7)
  void readReviews() throws JsonProcessingException {
    // login as the new user
    login(newUser.getUsername());
    int totalReviews = 0;
    double totalRating = 0;
    // read the reviews on a friend's recipe
    logger.info("******* user 21 recipe 1 *******");
    for (ReviewEntity review : readReviewsByRecipe(1L, false).getContent()) {
      assertThat(review.getAuthor().getId())
          .isIn(
              1L, 2L, 3L, 4L, 5L,
              21L); // the review's author must be one of the user's friends or the user themself
      assertThat(review.getRecipe().getId()).isEqualTo(1L);
      logger.info(
          "review for recipe: 1 with value: "
              + (1 + review.getReview().ordinal())
              + ", and content: "
              + review.getContent());
      totalReviews++;
      totalRating += (1 + review.getReview().ordinal());
    }
    logger.info("********************************");
    assertThat(readRecipe(1L, false).getRating()).isEqualTo(totalRating / totalReviews);

    for (ReviewEntity review : readReviews(false).getContent()) {
      assertThat(review.getAuthor().getId())
          .isIn(
              1L, 2L, 3L, 4L, 5L,
              21L); // the review's author must be one of the user's friends or the user themself
    }

    // attempt to read a non-visible review
    readReview(2L, true);

    logout();

    // login as a friend of the new user
    login("bevis_armada");
    // read the reviews on the new user's recipe
    totalReviews = 0;
    totalRating = 0;
    // read the reviews on a friend's recipe
    logger.info("******** user 1 recipe 5 *******");
    for (ReviewEntity review : readReviewsByRecipe(5L, false).getContent()) {
      assertThat(review.getAuthor().getId())
          .isIn(
              1L, 2L, 3L, 5L, 6L, 7L, 20L,
              21L); // the review's author must be one of the user's friends or the user themself
      logger.info(
          "review for recipe: 5 with value: "
              + (1 + review.getReview().ordinal())
              + ", and content: "
              + review.getContent());
      totalReviews++;
      totalRating += (1 + review.getReview().ordinal());
    }
    logger.info("********************************");
    assertThat(readRecipe(5L, false).getRating()).isEqualTo(totalRating / totalReviews);
    for (ReviewEntity review : readReviews(false).getContent()) {
      assertThat(review.getAuthor().getId())
          .isIn(
              1L, 2L, 3L, 5L, 6L, 7L, 20L,
              21L); // the review's author must be one of the user's friends or the user themself
    }
    logout();

    login("biggie");
    // read the reviews on the new user's recipe
    totalReviews = 0;
    totalRating = 0;
    // read the reviews on a friend's recipe
    logger.info("******** user 2 recipe 5 *******");
    for (ReviewEntity review : readReviewsByRecipe(5L, false).getContent()) {
      assertThat(review.getAuthor().getId())
          .isIn(
              1L, 2L, 4L, 5L, 7L, 8L, 15L,
              21L); // the review's author must be one of the user's friends or the user themself
      logger.info(
          "review for recipe: 5 with value: "
              + (1 + review.getReview().ordinal())
              + ", and content: "
              + review.getContent());
      totalReviews++;
      totalRating += (1 + review.getReview().ordinal());
    }
    logger.info("********************************");
    assertThat(readRecipe(5L, false).getRating()).isEqualTo(totalRating / totalReviews);
    logout();
  }

  @Test
  @Order(8)
  void deleteReview() throws JsonProcessingException {
    login(newUser.getUsername());
    List<Long> deletedReviews = new ArrayList<>();
    for (ReviewEntity reviewByUser : readReviewsByAuthor(21L, false).getContent()) {
      Long reviewtoDelete = reviewByUser.getId();
      ResponseEntity<Void> deleteResponse =
          restTemplate.exchange(
              "http://localhost:" + port + "/api/review/" + reviewtoDelete,
              HttpMethod.DELETE,
              null,
              Void.class);
      assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      deletedReviews.add(reviewtoDelete);
    }
    logout();

    login("bevis_armada");
    int totalReviews = 0;
    double totalRating = 0;
    // read the reviews on a friend's recipe
    logger.info("******** user 1 recipe 1 *******");
    for (ReviewEntity review : readReviewsByRecipe(1L, false).getContent()) {
      assertThat(review.getAuthor().getId())
          .isIn(
              1L, 2L, 3L, 5L, 6L, 7L,
              20L); // the review's author must be one of the user's friends or the user themself
      // (but user 21's review has been deleted)
      assertThat(review.getId()).isNotIn(deletedReviews);
      logger.info(
          "review for recipe: 1 with value: "
              + (1 + review.getReview().ordinal())
              + ", and content: "
              + review.getContent());
      totalReviews++;
      totalRating += (1 + review.getReview().ordinal());
    }
    logger.info("********************************");
    assertThat(readRecipe(1L, false).getRating()).isEqualTo(totalRating / totalReviews);
    logout();
  }

  @Test
  @Order(9)
  void deleteUser() throws JsonProcessingException {
    login("biggie");

    ResponseEntity<Void> deleteResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/user", HttpMethod.DELETE, null, Void.class);

    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    login("bevis_armada");
    int totalReviews = 0;
    double totalRating = 0;
    // read the reviews on a friend's recipe
    logger.info("******** user 1 recipe 1 *******");
    for (ReviewEntity review : readReviewsByRecipe(1L, false).getContent()) {
      assertThat(review.getAuthor().getId())
          .isIn(
              1L, 3L, 5L, 6L, 7L,
              20L); // the review's author must be one of the user's friends or the user themself
      // (but user 2's reviews have been deleted)
      logger.info(
          "review for recipe: 1 with value: "
              + (1 + review.getReview().ordinal())
              + ", and content: "
              + review.getContent());
      totalReviews++;
      totalRating += (1 + review.getReview().ordinal());
    }
    logger.info("********************************");
    assertThat(readRecipe(1L, false).getRating())
        .isEqualTo(Math.round((totalRating / totalReviews) * 10) / 10.0);
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

  private RecipeIngredientEntity getIngredient(String query, Double quantity, String unit) {
    String url = "http://localhost:" + port + "/api/ingredient/search?query=" + query;

    // Send GET request to the ingredient search endpoint
    ResponseEntity<List<IngredientEntity>> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<IngredientEntity>>() {});

    List<IngredientEntity> ingredients = response.getBody();

    if (ingredients != null && !ingredients.isEmpty()) {
      return new RecipeIngredientEntity(null, ingredients.get(0), quantity, unit);
    }

    return null;
  }

  private RecipeSkillEntity getSkill(String query) {
    String url = "http://localhost:" + port + "/api/skill/search?query=" + query;

    // Send GET request to the skill search endpoint
    ResponseEntity<List<SkillEntity>> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<SkillEntity>>() {});

    List<SkillEntity> skills = response.getBody();

    if (skills != null && !skills.isEmpty()) {
      return new RecipeSkillEntity(null, skills.get(0));
    }

    return null;
  }

  private RecipeEquipmentEntity getEquipment(String query) {
    String url = "http://localhost:" + port + "/api/equipment/search?query=" + query;

    // Send GET request to the ingredient search endpoint
    ResponseEntity<List<EquipmentEntity>> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<EquipmentEntity>>() {});

    List<EquipmentEntity> equipment = response.getBody();

    if (equipment != null && !equipment.isEmpty()) {
      return new RecipeEquipmentEntity(null, equipment.get(0));
    }

    return null;
  }

  private RecipeModel readRecipe(Long recipeId, boolean expectFailure) {
    ResponseEntity<RecipeModel> response =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/recipe/" + recipeId, RecipeModel.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      return response.getBody();
    }
  }

  private PagedModel<SimpleRecipeEntity> searchRecipes(String query, boolean expectFailure)
      throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/recipe/search?q=" + query;

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, null, String.class);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      // Deserialize JSON response to Page<SimpleRecipeEntity>
      ObjectMapper mapper = recipeMapperWithPageSupport();
      JavaType pageType =
          mapper
              .getTypeFactory()
              .constructParametricType(PagedModel.class, SimpleRecipeEntity.class);
      return mapper.readValue(response.getBody(), pageType);
    }
  }

  private ReviewEntity startCooking(Long recipe, boolean expectFailure) {
    ResponseEntity<ReviewEntity> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/recipe/" + recipe + "/cook", null, ReviewEntity.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      return response.getBody();
    }
  }

  private void addReview(ReviewEntity review, boolean expectFailure) {
    ResponseEntity<ReviewEntity> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/api/review", review, ReviewEntity.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.CREATED);
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
  }

  private ReviewEntity updateReview(ReviewEntity review, boolean expectFailure) {
    ResponseEntity<ReviewEntity> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/review/" + review.getId(),
            HttpMethod.PUT,
            new HttpEntity<>(review),
            ReviewEntity.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      return response.getBody();
    }
  }

  private PagedModel<ReviewEntity> readReviews(boolean expectFailure)
      throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/review";

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, null, String.class);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      // Deserialize JSON response to Page<SimpleRecipeEntity>
      ObjectMapper mapper = reviewMapperWithPageSupport();
      JavaType pageType =
          mapper.getTypeFactory().constructParametricType(PagedModel.class, ReviewEntity.class);
      return mapper.readValue(response.getBody(), pageType);
    }
  }

  private PagedModel<ReviewEntity> readReviewsByRecipe(Long recipeId, boolean expectFailure)
      throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/review/recipe/" + recipeId;

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, null, String.class);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      // Deserialize JSON response to Page<SimpleRecipeEntity>
      ObjectMapper mapper = reviewMapperWithPageSupport();
      JavaType pageType =
          mapper.getTypeFactory().constructParametricType(PagedModel.class, ReviewEntity.class);
      return mapper.readValue(response.getBody(), pageType);
    }
  }

  private PagedModel<ReviewEntity> readReviewsByAuthor(Long author, boolean expectFailure)
      throws JsonProcessingException {
    String url = "http://localhost:" + port + "/api/review/author/" + author;

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, null, String.class);

    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      // Deserialize JSON response to Page<SimpleRecipeEntity>
      ObjectMapper mapper = reviewMapperWithPageSupport();
      JavaType pageType =
          mapper.getTypeFactory().constructParametricType(PagedModel.class, ReviewEntity.class);
      return mapper.readValue(response.getBody(), pageType);
    }
  }

  private ReviewEntity readReview(Long reviewId, boolean expectFailure) {
    ResponseEntity<ReviewEntity> response =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/api/review/" + reviewId, ReviewEntity.class);
    if (expectFailure) {
      assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
      return null;
    } else {
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      return response.getBody();
    }
  }
}
