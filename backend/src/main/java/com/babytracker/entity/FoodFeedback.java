package com.babytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("food_feedback")
public class FoodFeedback {
    private Long id;
    private Long babyId;
    private Long recipeId;
    private String feedback;
}
