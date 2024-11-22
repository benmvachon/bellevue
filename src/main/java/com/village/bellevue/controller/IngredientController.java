package com.village.bellevue.controller;

import com.village.bellevue.entity.IngredientEntity;
import com.village.bellevue.service.IngredientService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingredient")
public class IngredientController {

  private final IngredientService ingredientService;

  public IngredientController(IngredientService ingredientService) {
    this.ingredientService = ingredientService;
  }

  @GetMapping("/search")
  public ResponseEntity<List<IngredientEntity>> search(@RequestParam String query) {
    List<IngredientEntity> ingredients = ingredientService.search(query);
    return ResponseEntity.ok(ingredients);
  }
}
