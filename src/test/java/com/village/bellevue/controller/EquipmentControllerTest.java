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
import com.village.bellevue.entity.EquipmentEntity;
import com.village.bellevue.service.EquipmentService;

public class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private EquipmentController equipmentController;
    @Mock
    private EquipmentService equipmentService;

    private final EquipmentEntity equipment1 = new EquipmentEntity(1L, "Hammer");
    private final EquipmentEntity equipment2 = new EquipmentEntity(2L, "Wrench");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(equipmentController).build();
    }

    @Test
    public void testSearchEquipmentSuccess() throws Exception {
        Gson gson = new Gson();

        List<EquipmentEntity> equipmentList = Arrays.asList(equipment1, equipment2);
        when(equipmentService.search(anyString())).thenReturn(equipmentList);

        mockMvc.perform(get("/api/equipment/search")
                .param("query", "tool")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(gson.toJson(equipmentList)));
    }

    @Test
    public void testSearchEquipmentNotFound() throws Exception {
        when(equipmentService.search(anyString())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/equipment/search")
                .param("query", "unknown")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }
}
