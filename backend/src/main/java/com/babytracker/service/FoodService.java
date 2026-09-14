package com.babytracker.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.babytracker.constants.ErrorCode;
import com.babytracker.constants.FeedbackTypes;
import com.babytracker.dto.FoodRecipeView;
import com.babytracker.entity.FoodFeedback;
import com.babytracker.entity.FoodRecipe;
import com.babytracker.exception.BizException;
import com.babytracker.mapper.FoodFeedbackMapper;
import com.babytracker.mapper.FoodMapper;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FoodService {
    private final FoodMapper mapper;
    private final FoodFeedbackMapper feedbackMapper;
    public FoodService(FoodMapper mapper, FoodFeedbackMapper feedbackMapper) {
        this.mapper = mapper;
        this.feedbackMapper = feedbackMapper;
    }
    public List<FoodRecipeView> recommend(Integer monthAge, String allergen, Long babyId) {
        QueryWrapper<FoodRecipe> query = new QueryWrapper<FoodRecipe>().le("month_age_min", monthAge).ge("month_age_max", monthAge);
        if (allergen != null && !allergen.isBlank()) query.and(w -> w.isNull("allergens").or().notLike("allergens", allergen));
        List<FoodRecipe> recipes = mapper.selectList(query);
        Map<Long, String> feedbackByRecipe = babyId == null ? Collections.emptyMap() : feedbackMap(babyId);
        return recipes.stream().map(recipe -> FoodRecipeView.of(recipe, feedbackByRecipe.get(recipe.getId()))).collect(Collectors.toList());
    }
    public FoodFeedback saveFeedback(FoodFeedback feedback) {
        if (feedback == null || feedback.getBabyId() == null || feedback.getRecipeId() == null
                || feedback.getFeedback() == null || !FeedbackTypes.ALL.contains(feedback.getFeedback())) {
            throw new BizException(ErrorCode.VALIDATION_FAILED, "反馈参数不正确");
        }
        FoodFeedback existing = feedbackMapper.selectOne(byBabyAndRecipe(feedback.getBabyId(), feedback.getRecipeId()));
        if (existing == null) {
            feedback.setId(null);
            feedbackMapper.insert(feedback);
            return feedback;
        }
        existing.setFeedback(feedback.getFeedback());
        feedbackMapper.updateById(existing);
        return existing;
    }
    public void removeFeedback(Long babyId, Long recipeId) {
        if (babyId == null || recipeId == null) throw new BizException(ErrorCode.VALIDATION_FAILED, "反馈参数不正确");
        feedbackMapper.delete(byBabyAndRecipe(babyId, recipeId));
    }
    private Map<Long, String> feedbackMap(Long babyId) {
        return feedbackMapper.selectList(new QueryWrapper<FoodFeedback>().eq("baby_id", babyId)).stream()
                .collect(Collectors.toMap(FoodFeedback::getRecipeId, FoodFeedback::getFeedback));
    }
    private QueryWrapper<FoodFeedback> byBabyAndRecipe(Long babyId, Long recipeId) {
        return new QueryWrapper<FoodFeedback>().eq("baby_id", babyId).eq("recipe_id", recipeId);
    }
}
