package com.village.bellevue.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.ReviewEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.RecipeException;
import com.village.bellevue.error.ReviewException;
import com.village.bellevue.model.RecipeModel;
import com.village.bellevue.service.RecipeService;
import com.village.bellevue.service.ReviewService;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final RecipeService recipeService;
    private final ReviewService reviewService;

    public RecipeController(RecipeService recipeService, ReviewService reviewService) {
        this.recipeService = recipeService;
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<RecipeModel> create(@RequestBody RecipeEntity recipe) {
        try {
            RecipeModel createdRecipe = recipeService.create(recipe);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RecipeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeModel> read(@PathVariable Long id) {
        try {
            Optional<RecipeModel> recipe = recipeService.read(id);
            return recipe.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RecipeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}/rating")
    public ResponseEntity<Double> readRating(@PathVariable Long id) {
        try {
            Optional<AggregateRatingEntity> rating = recipeService.readRating(id);
            return rating.map((value) -> ResponseEntity.ok(value.getRating())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/ratings")
    public ResponseEntity<List<AggregateRatingEntity>> readRatings(@RequestParam("id") List<Long> ids) {
        try {
            List<AggregateRatingEntity> ratings = new ArrayList<>();
            for (Long id : ids) {
                Optional<AggregateRatingEntity> rating = recipeService.readRating(id);
                if (rating.isPresent()) {
                    ratings.add(rating.get());
                }
            }
            return ResponseEntity.ok(ratings);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<Page<SimpleRecipeEntity>> readAll(
            @PathVariable Long author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Page<SimpleRecipeEntity> recipes = recipeService.readAll(author, page, size);
            return ResponseEntity.status(HttpStatus.OK).body(recipes);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RecipeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeModel> update(@PathVariable Long id, @RequestBody RecipeEntity updatedRecipe) {
        try {
            RecipeModel recipe = recipeService.update(id, updatedRecipe);
            return ResponseEntity.ok(recipe);
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RecipeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/cook/{id}")
    public ResponseEntity<ReviewEntity> cook(@PathVariable Long id) {
        try {
            Optional<ReviewEntity> createdReview = reviewService.read(recipeService.cook(id));
            return createdReview.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RecipeException | ReviewException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            recipeService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RecipeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<PagedModel<SimpleRecipeEntity>> search(
            @RequestParam("q") String query,
            @RequestParam(name = "i", required = false) List<Long> ingredients,
            @RequestParam(name = "s", required = false) List<Long> skills,
            @RequestParam(name = "e", required = false) List<Long> equipment,
            @RequestParam(name = "p", defaultValue = "0") int page,
            @RequestParam(name = "n", defaultValue = "5") int size) {
        try {
            Page<SimpleRecipeEntity> recipes = recipeService.search(query, ingredients, skills, equipment, page, size);
            return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(recipes));
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RecipeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
