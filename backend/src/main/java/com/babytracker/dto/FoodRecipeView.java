package com.babytracker.dto;

import com.babytracker.entity.FoodRecipe;
import lombok.Data;

@Data
public class FoodRecipeView {
    private Long id;
    private Integer monthAgeMin;
    private Integer monthAgeMax;
    private String name;
    private String ingredients;
    private String steps;
    private String nutrition;
    private String allergens;
    /** 当前宝宝的反馈：like / neutral / dislike，未标记为 null */
    private String feedback;

    public static FoodRecipeView of(FoodRecipe recipe, String feedback) {
        FoodRecipeView view = new FoodRecipeView();
        view.setId(recipe.getId());
        view.setMonthAgeMin(recipe.getMonthAgeMin());
        view.setMonthAgeMax(recipe.getMonthAgeMax());
        view.setName(recipe.getName());
        view.setIngredients(recipe.getIngredients());
        view.setSteps(recipe.getSteps());
        view.setNutrition(recipe.getNutrition());
        view.setAllergens(recipe.getAllergens());
        view.setFeedback(feedback);
        return view;
    }
}
