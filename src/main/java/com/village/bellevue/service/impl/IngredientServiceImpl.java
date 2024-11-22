package com.village.bellevue.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.village.bellevue.entity.IngredientEntity;
import com.village.bellevue.repository.IngredientRepository;
import com.village.bellevue.service.IngredientService;

@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientServiceImpl(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Override
    public List<IngredientEntity> search(String query) {
        return ingredientRepository.findByNameStartingWithIgnoreCase(query);
    }
}
