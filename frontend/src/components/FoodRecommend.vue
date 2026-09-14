<template>
  <div>
    <div v-if="babies.length" class="baby-bar">
      <span class="baby-bar-label">当前宝宝</span>
      <van-tag
        v-for="baby in babies"
        :key="baby.id"
        size="large"
        :type="baby.id === currentBabyId ? 'primary' : 'default'"
        class="baby-chip"
        @click="switchBaby(baby)"
      >{{ baby.name }}</van-tag>
    </div>
    <van-empty v-if="!babies.length && loaded" description="暂无宝宝档案，请先创建" />
    <template v-if="currentBaby">
      <div class="filter-bar">
        <span class="filter-label">月龄</span>
        <van-stepper v-model="monthAge" min="6" max="36" @change="loadFoods" />
        <span class="filter-label">个月</span>
      </div>
      <van-search v-model="allergen" placeholder="输入过敏原筛选，如 鸡蛋" @search="loadFoods" @clear="loadFoods" />
      <van-empty v-if="!foods.length && foodsLoaded" description="暂无符合条件的食谱" />
      <FoodItem v-for="food in visibleFoods" :key="food.id" :food="food" @toggle="toggleFeedback" />
      <van-collapse v-if="dislikedFoods.length" v-model="dislikeOpen" class="dislike-collapse">
        <van-collapse-item :title="`不喜欢的食谱（${dislikedFoods.length}）`" name="dislike">
          <FoodItem v-for="food in dislikedFoods" :key="food.id" :food="food" @toggle="toggleFeedback" />
        </van-collapse-item>
      </van-collapse>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { showToast } from 'vant';
import { clearFeedback, fetchBabies, fetchRecommendations, saveFeedback } from '../api';
import type { Baby, FeedbackType, FoodRecipe } from '../types';
import FoodItem from './FoodItem.vue';

const babies = ref<Baby[]>([]);
const currentBabyId = ref<string | null>(null);
const monthAge = ref(10);
const allergen = ref('');
const foods = ref<FoodRecipe[]>([]);
const loaded = ref(false);
const foodsLoaded = ref(false);
const dislikeOpen = ref<string[]>([]);

const currentBaby = computed(() => babies.value.find(b => b.id === currentBabyId.value) ?? null);
const visibleFoods = computed(() => foods.value.filter(f => f.feedback !== 'dislike'));
const dislikedFoods = computed(() => foods.value.filter(f => f.feedback === 'dislike'));

const calcMonthAge = (birthday: string) => {
  const birth = new Date(birthday);
  const now = new Date();
  let months = (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth());
  if (now.getDate() < birth.getDate()) months -= 1;
  return Math.min(36, Math.max(6, months));
};

const loadFoods = async () => {
  if (currentBabyId.value == null) return;
  try {
    foods.value = await fetchRecommendations(currentBabyId.value, monthAge.value, allergen.value);
  } catch {
    showToast('食谱加载失败');
  } finally {
    foodsLoaded.value = true;
  }
};

const switchBaby = (baby: Baby) => {
  if (baby.id === currentBabyId.value) return;
  currentBabyId.value = baby.id;
  monthAge.value = calcMonthAge(baby.birthday);
  dislikeOpen.value = [];
  loadFoods();
};

const toggleFeedback = async (food: FoodRecipe, type: FeedbackType) => {
  if (currentBabyId.value == null) return;
  const previous = food.feedback ?? null;
  const next = previous === type ? null : type;
  food.feedback = next;
  try {
    if (next === null) await clearFeedback(currentBabyId.value, food.id);
    else await saveFeedback(currentBabyId.value, food.id, next);
  } catch {
    food.feedback = previous;
    showToast('反馈保存失败');
  }
};

onMounted(async () => {
  try {
    babies.value = await fetchBabies();
    if (babies.value.length) switchBaby(babies.value[0]);
  } catch {
    showToast('宝宝列表加载失败');
  } finally {
    loaded.value = true;
  }
});
</script>
