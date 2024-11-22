package com.village.bellevue.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.entity.SkillEntity;
import com.village.bellevue.service.SkillService;

@RestController
@RequestMapping("/api/skill")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SkillEntity>> search(@RequestParam String query) {
        List<SkillEntity> skills = skillService.search(query);
        return ResponseEntity.ok(skills);
    }
}
