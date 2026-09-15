package com.babytracker;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.babytracker.constants.FeedbackTypes;
import com.babytracker.entity.Baby;
import com.babytracker.entity.FoodFeedback;
import com.babytracker.entity.FoodRecipe;
import com.babytracker.mapper.BabyMapper;
import com.babytracker.mapper.FoodFeedbackMapper;
import com.babytracker.mapper.FoodMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * 食谱反馈并发写入测试。
 * 并发请求在独立线程中自动提交，因此本类不使用测试事务，
 * 每个用例结束后显式清理自己创建的数据，保证可重复运行。
 */
@SpringBootTest
@AutoConfigureMockMvc
class FoodFeedbackConcurrencyTest {
    private static final int THREADS = 16;

    @Autowired private MockMvc mvc;
    @Autowired private BabyMapper babyMapper;
    @Autowired private FoodMapper foodMapper;
    @Autowired private FoodFeedbackMapper feedbackMapper;

    private Baby baby;
    private FoodRecipe recipe;

    @BeforeEach
    void setUp() {
        baby = new Baby();
        baby.setName("并发宝宝");
        baby.setBirthday(LocalDate.of(2025, 1, 1));
        babyMapper.insert(baby);
        recipe = new FoodRecipe();
        recipe.setName("并发测试食谱");
        recipe.setMonthAgeMin(6);
        recipe.setMonthAgeMax(12);
        foodMapper.insert(recipe);
    }

    @AfterEach
    void tearDown() {
        feedbackMapper.delete(new QueryWrapper<FoodFeedback>().eq("baby_id", baby.getId()));
        babyMapper.deleteById(baby.getId());
        foodMapper.deleteById(recipe.getId());
    }

    @Test
    void concurrentSameFeedbackKeepsSingleRow() throws Exception {
        String[] values = new String[THREADS];
        Arrays.fill(values, "like");
        List<String> bodies = submitConcurrent(values);
        for (String body : bodies) {
            assertFalse(body.contains("\"success\":false"), "并发写入不应返回错误: " + body);
            assertTrue(body.contains("\"feedback\":\"like\""), "响应应包含反馈结果: " + body);
        }
        assertEquals(1L, feedbackCount(), "并发写入最终只应保留一条反馈");
    }

    @Test
    void concurrentMixedFeedbackKeepsSingleRow() throws Exception {
        String[] values = new String[THREADS];
        for (int i = 0; i < THREADS; i++) values[i] = i % 2 == 0 ? FeedbackTypes.LIKE : FeedbackTypes.DISLIKE;
        List<String> bodies = submitConcurrent(values);
        for (String body : bodies) {
            assertFalse(body.contains("\"success\":false"), "并发写入不应返回错误: " + body);
        }
        assertEquals(1L, feedbackCount(), "并发写入最终只应保留一条反馈");
        FoodFeedback row = feedbackMapper.selectOne(new QueryWrapper<FoodFeedback>()
                .eq("baby_id", baby.getId()).eq("recipe_id", recipe.getId()));
        assertNotNull(row);
        assertTrue(FeedbackTypes.ALL.contains(row.getFeedback()), "最终反馈值应合法: " + row.getFeedback());
    }

    /** 多线程同时提交同一宝宝同一食谱的反馈，返回各请求的响应体 */
    private List<String> submitConcurrent(String[] values) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(values.length);
        try {
            CountDownLatch ready = new CountDownLatch(values.length);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<String>> futures = new ArrayList<>();
            for (String value : values) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await(10, TimeUnit.SECONDS);
                    return mvc.perform(put("/api/foods/feedback").contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"babyId\":\"" + baby.getId() + "\",\"recipeId\":\"" + recipe.getId()
                                            + "\",\"feedback\":\"" + value + "\"}"))
                            .andReturn().getResponse().getContentAsString();
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS), "线程未就绪");
            start.countDown();
            List<String> bodies = new ArrayList<>();
            for (Future<String> future : futures) bodies.add(future.get(30, TimeUnit.SECONDS));
            return bodies;
        } finally {
            pool.shutdownNow();
        }
    }

    private long feedbackCount() {
        return feedbackMapper.selectCount(new QueryWrapper<FoodFeedback>()
                .eq("baby_id", baby.getId()).eq("recipe_id", recipe.getId()));
    }
}
