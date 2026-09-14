package com.babytracker.controller;

import com.babytracker.dto.FoodRecipeView;
import com.babytracker.entity.FoodFeedback;
import com.babytracker.service.FoodService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {
    private final FoodService service;
    public FoodController(FoodService service) { this.service = service; }
    @GetMapping("/recommend") public List<FoodRecipeView> recommend(@RequestParam Integer monthAge, @RequestParam(required = false) String allergen, @RequestParam(required = false) Long babyId) { return service.recommend(monthAge, allergen, babyId); }
    @PutMapping("/feedback") public FoodFeedback saveFeedback(@RequestBody FoodFeedback feedback) { return service.saveFeedback(feedback); }
    @DeleteMapping("/feedback") public void removeFeedback(@RequestParam Long babyId, @RequestParam Long recipeId) { service.removeFeedback(babyId, recipeId); }
}
