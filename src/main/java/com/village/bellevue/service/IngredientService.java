package com.village.bellevue.service;

import com.village.bellevue.entity.IngredientEntity;
import java.util.List;

public interface IngredientService {

  public List<IngredientEntity> search(String query);
}
