package com.village.bellevue.service.impl;

import static com.village.bellevue.config.CacheConfig.RECIPE_SECURITY_CACHE_NAME;
import static com.village.bellevue.config.CacheConfig.evictKeysByPattern;
import static com.village.bellevue.config.CacheConfig.getEntityCacheKeyPattern;
import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.entity.FriendEntity.FriendshipStatus;
import com.village.bellevue.entity.IngredientEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.RecipeEquipmentEntity;
import com.village.bellevue.entity.RecipeIngredientEntity;
import com.village.bellevue.entity.RecipeSkillEntity;
import com.village.bellevue.entity.RecipeStepEntity;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;
import com.village.bellevue.entity.SkillEntity;
import com.village.bellevue.entity.id.AggregateRatingId;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.error.RecipeException;
import com.village.bellevue.model.RecipeModel;
import com.village.bellevue.repository.AggregateRatingRepository;
import com.village.bellevue.repository.EquipmentRepository;
import com.village.bellevue.repository.IngredientRepository;
import com.village.bellevue.repository.RecipeRepository;
import com.village.bellevue.repository.SkillRepository;
import com.village.bellevue.service.FriendService;
import com.village.bellevue.service.RecipeService;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeServiceImpl implements RecipeService {

  public static final String CAN_READ_CACHE_KEY = "canRead";
  public static final String CAN_UPDATE_CACHE_KEY = "canUpdate";

  private final RecipeRepository recipeRepository;

  private final IngredientRepository ingredientRepository;
  private final SkillRepository skillRepository;
  private final EquipmentRepository equipmentRepository;

  private final AggregateRatingRepository ratingRepository;
  private final FriendService friendService;
  private final RedisTemplate<String, Object> redisTemplate;

  private final DataSource dataSource;

  public RecipeServiceImpl(
      RecipeRepository recipeRepository,
      IngredientRepository ingredientRepository,
      SkillRepository skillRepository,
      EquipmentRepository equipmentRepository,
      AggregateRatingRepository ratingRepository,
      FriendService friendService,
      RedisTemplate<String, Object> redisTemplate,
      DataSource dataSource) {
    this.recipeRepository = recipeRepository;

    this.ingredientRepository = ingredientRepository;
    this.skillRepository = skillRepository;
    this.equipmentRepository = equipmentRepository;

    this.ratingRepository = ratingRepository;
    this.friendService = friendService;
    this.redisTemplate = redisTemplate;

    this.dataSource = dataSource;
  }

  @Transactional
  @Override
  public RecipeModel create(RecipeEntity recipe) throws AuthorizationException, RecipeException {
    return new RecipeModel(save(recipe), Optional.empty());
  }

  @Override
  public Optional<RecipeModel> read(Long id) throws AuthorizationException, RecipeException {
    if (canRead(id)) {
      Optional<RecipeEntity> recipe = recipeRepository.findById(id);
      if (recipe.isPresent()) {
        RecipeEntity recipeEntity = recipe.get();
        return Optional.of(
            new RecipeModel(
                recipeEntity,
                ratingRepository.findById(new AggregateRatingId(getAuthenticatedUserId(), id))));
      }
      return Optional.empty();
    }
    throw new AuthorizationException(
        "Currently authenticated user is not authorized to read recipe");
  }

  @Override
  public Optional<AggregateRatingEntity> readRating(Long id) throws AuthorizationException {
    if (canRead(id)) {
      return ratingRepository.findById(new AggregateRatingId(getAuthenticatedUserId(), id));
    }
    throw new AuthorizationException(
        "Currently authenticated user is not authorized to read recipe");
  }

  @Override
  public Page<SimpleRecipeEntity> readAll(Long author, int page, int size)
      throws AuthorizationException, RecipeException {
    try {
      Optional<FriendshipStatus> status = friendService.getStatus(author);
      if (status.isEmpty() || !FriendshipStatus.ACCEPTED.equals(status.get())) {
        throw new AuthorizationException(
            "Currently authenticated user is not authorized to read recipes");
      }
      return recipeRepository.findByAuthorId(author, PageRequest.of(page, size));
    } catch (FriendshipException e) {
      throw new RecipeException("User not found: " + author);
    }
  }

  @Transactional
  @Override
  public RecipeModel update(Long id, RecipeEntity updatedRecipe)
      throws AuthorizationException, RecipeException {
    if (!canUpdate(id)) {
      throw new AuthorizationException(
          "Currently authenticated user is not authorized to update recipe: " + id);
    }
    // ensure that the correct recipe is being updated
    updatedRecipe.setId(id);

    return new RecipeModel(
        save(updatedRecipe),
        ratingRepository.findById(new AggregateRatingId(getAuthenticatedUserId(), id)));
  }

  @Override
  @Transactional
  public Long cook(Long recipe) throws AuthorizationException, RecipeException {
    if (!canRead(recipe)) {
      throw new AuthorizationException(
          "Currently authenticated user is not authorized to review recipe");
    }
    try (Connection connection = dataSource.getConnection();
        CallableStatement stmt = connection.prepareCall("{call start_cooking(?, ?, ?)}")) {
      stmt.setLong(1, getAuthenticatedUserId());
      stmt.setLong(2, recipe);
      stmt.registerOutParameter(3, java.sql.Types.INTEGER);
      stmt.executeUpdate();
      return stmt.getLong(3);
    } catch (SQLException e) {
      throw new RecipeException("Failed to start cook. SQL command error: " + e.getMessage(), e);
    }
  }

  @Override
  public void delete(Long id) throws AuthorizationException, RecipeException {
    if (!canUpdate(id)) {
      throw new AuthorizationException(
          "Currently authenticated user is not authorized to delete recipe: " + id);
    }
    try {
      recipeRepository.deleteById(id);
    } finally {
      evictRecipeSecurityCaches(id);
    }
  }

  @Override
  public Page<SimpleRecipeEntity> search(
      String query,
      List<Long> ingredients,
      List<Long> skills,
      List<Long> equipment,
      int page,
      int size)
      throws AuthorizationException, RecipeException {
    return recipeRepository.findByNameStartingWithIgnoreCase(
        query,
        getAuthenticatedUserId(),
        ingredients,
        skills,
        equipment,
        PageRequest.of(page, size));
  }

  @Cacheable(
      value = RECIPE_SECURITY_CACHE_NAME,
      key =
          "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.RecipeServiceImpl).CAN_READ_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #id)")
  @Override
  public boolean canRead(Long id) {
    return recipeRepository.canRead(id, getAuthenticatedUserId());
  }

  @Cacheable(
      value = RECIPE_SECURITY_CACHE_NAME,
      key =
          "T(com.village.bellevue.config.CacheConfig).getCacheKey(T(com.village.bellevue.service.impl.RecipeServiceImpl).CAN_UPDATE_CACHE_KEY, T(com.village.bellevue.config.security.SecurityConfig).getAuthenticatedUserId(), #id)")
  @Override
  public boolean canUpdate(Long id) {
    return recipeRepository.canUpdate(id, getAuthenticatedUserId());
  }

  private void evictRecipeSecurityCaches(Long id) {
    evictKeysByPattern(
        redisTemplate,
        RECIPE_SECURITY_CACHE_NAME,
        getEntityCacheKeyPattern(CAN_READ_CACHE_KEY, id));
    evictKeysByPattern(
        redisTemplate,
        RECIPE_SECURITY_CACHE_NAME,
        getEntityCacheKeyPattern(CAN_UPDATE_CACHE_KEY, id));
  }

  private RecipeEntity save(RecipeEntity recipe) {
    ScrubbedUserEntity author = new ScrubbedUserEntity();
    author.setId(getAuthenticatedUserId());
    recipe.setAuthor(author);

    // Temporarily detach related entities
    Set<RecipeStepEntity> steps = new HashSet<>();
    steps.addAll(recipe.getSteps());
    Set<RecipeIngredientEntity> ingredients = new HashSet<>();
    ingredients.addAll(recipe.getIngredients());
    Set<RecipeSkillEntity> skills = new HashSet<>();
    skills.addAll(recipe.getSkills());
    Set<RecipeEquipmentEntity> equipment = new HashSet<>();
    equipment.addAll(recipe.getEquipment());

    if (recipe.getSteps() != null) recipe.getSteps().clear();
    if (recipe.getIngredients() != null) recipe.getIngredients().clear();
    if (recipe.getSkills() != null) recipe.getSkills().clear();
    if (recipe.getEquipment() != null) recipe.getEquipment().clear();

    // Save and flush the recipe entity first to avoid detached errors
    final RecipeEntity savedRecipe = recipeRepository.saveAndFlush(recipe);
    savedRecipe.getSteps().addAll(steps);
    savedRecipe.getIngredients().addAll(ingredients);
    savedRecipe.getSkills().addAll(skills);
    savedRecipe.getEquipment().addAll(equipment);

    bindRelatedEntities(savedRecipe);

    // Persist the updated recipe with all entities correctly associated
    return recipeRepository.save(savedRecipe);
  }

  private RecipeEntity bindRelatedEntities(RecipeEntity recipeEntity) {
    // ensure the foreign key on the related entities points back to this recipe
    if (recipeEntity.getSteps() != null) {
      recipeEntity.getSteps().forEach(step -> step.setRecipe(recipeEntity.getId()));
    }
    if (recipeEntity.getIngredients() != null) {
      recipeEntity
          .getIngredients()
          .forEach(
              (ingredient) -> {
                IngredientEntity managedIngredient =
                    ingredientRepository.findById(ingredient.getIngredient().getId()).get();
                ingredient.setIngredient(managedIngredient);
                ingredient.setRecipe(recipeEntity.getId());
              });
    }
    if (recipeEntity.getSkills() != null) {
      recipeEntity
          .getSkills()
          .forEach(
              (skill) -> {
                SkillEntity managedSkill = skillRepository.findById(skill.getSkill().getId()).get();
                skill.setSkill(managedSkill);
                skill.setRecipe(recipeEntity.getId());
              });
    }
    if (recipeEntity.getEquipment() != null) {
      recipeEntity
          .getEquipment()
          .forEach(
              (equipmentEntity) -> {
                EquipmentEntity managedEquipment =
                    equipmentRepository.findById(equipmentEntity.getEquipment().getId()).get();
                equipmentEntity.setEquipment(managedEquipment);
                equipmentEntity.setRecipe(recipeEntity.getId());
              });
    }
    return recipeEntity;
  }
}
