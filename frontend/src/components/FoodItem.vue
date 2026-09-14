<template>
  <div class="food-item">
    <div class="food-head">
      <span class="food-name">{{ food.name }}</span>
      <van-tag type="primary" plain>适合 {{ food.monthAgeMin }}-{{ food.monthAgeMax }} 个月</van-tag>
    </div>
    <p v-if="food.ingredients" class="food-meta">食材：{{ food.ingredients }}</p>
    <p v-if="food.allergens" class="food-meta">过敏原：{{ food.allergens }}</p>
    <div class="feedback-bar">
      <van-button
        v-for="opt in options"
        :key="opt.value"
        size="small"
        round
        :type="food.feedback === opt.value ? opt.activeType : 'default'"
        :plain="food.feedback !== opt.value"
        @click="emit('toggle', food, opt.value)"
      >{{ opt.label }}</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FeedbackType, FoodRecipe } from '../types';

defineProps<{ food: FoodRecipe }>();
const emit = defineEmits<{ (e: 'toggle', food: FoodRecipe, type: FeedbackType): void }>();

const options: { value: FeedbackType; label: string; activeType: 'primary' | 'warning' | 'danger' }[] = [
  { value: 'like', label: '😍 喜欢', activeType: 'primary' },
  { value: 'neutral', label: '🙂 一般', activeType: 'warning' },
  { value: 'dislike', label: '🙁 不喜欢', activeType: 'danger' },
];
</script>
