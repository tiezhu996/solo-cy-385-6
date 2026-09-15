package com.babytracker;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.babytracker.entity.Baby;
import com.babytracker.entity.FoodFeedback;
import com.babytracker.entity.FoodRecipe;
import com.babytracker.mapper.BabyMapper;
import com.babytracker.mapper.FoodFeedbackMapper;
import com.babytracker.mapper.FoodMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 食谱反馈接口集成测试。
 * 使用 H2 内存库（MySQL 模式），无需外部服务即可独立启动；
 * 每个用例在事务中执行、结束后回滚，自动清理测试数据，可重复运行。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FoodFeedbackApiTest {
    /** JS Number 安全整数上限 2^53-1，雪花 ID 超出该范围 */
    private static final long JS_SAFE_INTEGER_MAX = 9007199254740991L;
    /** 明显不存在的 ID（雪花 ID 为大数，小数值不会命中任何记录） */
    private static final long NON_EXISTENT_ID = 999999999L;

    @Autowired private MockMvc mvc;
    @Autowired private BabyMapper babyMapper;
    @Autowired private FoodMapper foodMapper;
    @Autowired private FoodFeedbackMapper feedbackMapper;

    private Baby babyA;
    private Baby babyB;
    private FoodRecipe recipe;

    @BeforeEach
    void setUp() {
        babyA = newBaby("宝宝甲");
        babyB = newBaby("宝宝乙");
        recipe = newRecipe("测试南瓜糊", 6, 12);
    }

    @Test
    void longBabyIdSavedAndReadBackPrecisely() throws Exception {
        assertTrue(babyA.getId() > JS_SAFE_INTEGER_MAX, "雪花 ID 应超出 JS 安全整数范围");
        String babyId = String.valueOf(babyA.getId());
        String recipeId = String.valueOf(recipe.getId());

        String putBody = putFeedback(babyId, recipeId, "dislike")
                .andExpect(jsonPath("$.feedback").value("dislike"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(putBody.contains("\"babyId\":\"" + babyId + "\""),
                "响应中的宝宝 ID 应为精确字符串，实际: " + putBody);

        String recommend = mvc.perform(get("/api/foods/recommend").param("monthAge", "10").param("babyId", babyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].feedback").value("dislike"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(recommend.contains("\"id\":\"" + recipeId + "\""),
                "推荐结果中的食谱 ID 应为精确字符串，实际: " + recommend);
    }

    @Test
    void feedbackIsolatedBetweenBabies() throws Exception {
        putFeedback(id(babyA), id(recipe), "dislike").andExpect(jsonPath("$.feedback").value("dislike"));

        mvc.perform(get("/api/foods/recommend").param("monthAge", "10").param("babyId", id(babyA)))
                .andExpect(jsonPath("$[0].feedback").value("dislike"));
        mvc.perform(get("/api/foods/recommend").param("monthAge", "10").param("babyId", id(babyB)))
                .andExpect(jsonPath("$[0].feedback").value(nullValue()));
    }

    @Test
    void remarkUpdatesExistingFeedback() throws Exception {
        putFeedback(id(babyA), id(recipe), "dislike");
        putFeedback(id(babyA), id(recipe), "like");

        mvc.perform(get("/api/foods/recommend").param("monthAge", "10").param("babyId", id(babyA)))
                .andExpect(jsonPath("$[0].feedback").value("like"));
        assertEquals(1L, feedbackCount(babyA.getId(), recipe.getId()), "重复标记应更新同一行而非新增");
    }

    @Test
    void clearFeedbackReturnsToUnmarked() throws Exception {
        putFeedback(id(babyA), id(recipe), "dislike");
        mvc.perform(delete("/api/foods/feedback").param("babyId", id(babyA)).param("recipeId", id(recipe)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/foods/recommend").param("monthAge", "10").param("babyId", id(babyA)))
                .andExpect(jsonPath("$[0].feedback").value(nullValue()));
        assertEquals(0L, feedbackCount(babyA.getId(), recipe.getId()), "取消标记后不应残留反馈行");
    }

    @Test
    void rejectFeedbackForUnknownBaby() throws Exception {
        putFeedback(String.valueOf(NON_EXISTENT_ID), id(recipe), "like")
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        assertEquals(0L, feedbackCount(NON_EXISTENT_ID, recipe.getId()), "无效宝宝的反馈不应写入");
    }

    @Test
    void rejectFeedbackForUnknownRecipe() throws Exception {
        putFeedback(id(babyA), String.valueOf(NON_EXISTENT_ID), "like")
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        assertEquals(0L, feedbackCount(babyA.getId(), NON_EXISTENT_ID), "无效食谱的反馈不应写入");
    }

    @Test
    void rejectIllegalFeedbackValue() throws Exception {
        putFeedback(id(babyA), id(recipe), "love")
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mvc.perform(put("/api/foods/feedback").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":\"" + id(recipe) + "\",\"feedback\":\"like\"}"))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        assertEquals(0L, feedbackCount(babyA.getId(), recipe.getId()), "非法反馈不应写入");
    }

    private Baby newBaby(String name) {
        Baby baby = new Baby();
        baby.setName(name);
        baby.setBirthday(LocalDate.of(2025, 1, 1));
        babyMapper.insert(baby);
        assertNotNull(baby.getId());
        return baby;
    }

    private FoodRecipe newRecipe(String name, int monthMin, int monthMax) {
        FoodRecipe foodRecipe = new FoodRecipe();
        foodRecipe.setName(name);
        foodRecipe.setMonthAgeMin(monthMin);
        foodRecipe.setMonthAgeMax(monthMax);
        foodMapper.insert(foodRecipe);
        assertNotNull(foodRecipe.getId());
        return foodRecipe;
    }

    /** 以前端相同的方式提交反馈：ID 作为 JSON 字符串 */
    private org.springframework.test.web.servlet.ResultActions putFeedback(String babyId, String recipeId, String feedback) throws Exception {
        return mvc.perform(put("/api/foods/feedback").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"babyId\":\"" + babyId + "\",\"recipeId\":\"" + recipeId + "\",\"feedback\":\"" + feedback + "\"}"))
                .andExpect(status().isOk());
    }

    private long feedbackCount(long babyId, long recipeId) {
        return feedbackMapper.selectCount(new QueryWrapper<FoodFeedback>()
                .eq("baby_id", babyId).eq("recipe_id", recipeId));
    }

    private String id(Baby baby) { return String.valueOf(baby.getId()); }
    private String id(FoodRecipe foodRecipe) { return String.valueOf(foodRecipe.getId()); }
}
