package com.village.bellevue.service;

import java.util.List;

import com.village.bellevue.entity.IngredientEntity;

public interface IngredientService {

    public List<IngredientEntity> search(String query);
}
