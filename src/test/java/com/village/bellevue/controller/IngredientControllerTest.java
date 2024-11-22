package com.village.bellevue.controller;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.Gson;
import com.village.bellevue.entity.IngredientEntity;
import com.village.bellevue.service.IngredientService;

public class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private IngredientController IngredientController;
    @Mock
    private IngredientService IngredientService;

    private final IngredientEntity ingredient1 = new IngredientEntity(1L, "Sugar", false, false, false, false, null);
    private final IngredientEntity ingredient2 = new IngredientEntity(2L, "Spice", false, false, false, false, null);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(IngredientController).build();
    }

    @Test
    public void testSearchIngredientSuccess() throws Exception {
        Gson gson = new Gson();
        List<IngredientEntity> ingredientList = Arrays.asList(ingredient1, ingredient2);
        when(IngredientService.search(anyString())).thenReturn(ingredientList);

        mockMvc.perform(get("/api/ingredient/search")
                .param("query", "tool")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(gson.toJson(ingredientList)));
    }

    @Test
    public void testSearchIngredientNotFound() throws Exception {
        when(IngredientService.search(anyString())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/ingredient/search")
                .param("query", "unknown")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }
}
