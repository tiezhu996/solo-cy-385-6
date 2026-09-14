import type { Baby, FeedbackType, FoodRecipe } from '../types';

const request = async <T>(url: string, options?: RequestInit): Promise<T> => {
  const res = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (!res.ok) throw new Error(`请求失败（${res.status}）`);
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (data && data.success === false) throw new Error(data.message || '请求失败');
  return data as T;
};

export const fetchBabies = () => request<Baby[]>('/api/babies');

export const fetchRecommendations = (babyId: string, monthAge: number, allergen: string) => {
  const params = new URLSearchParams({ monthAge: String(monthAge), babyId });
  if (allergen.trim()) params.set('allergen', allergen.trim());
  return request<FoodRecipe[]>(`/api/foods/recommend?${params.toString()}`);
};

export const saveFeedback = (babyId: string, recipeId: string, feedback: FeedbackType) =>
  request('/api/foods/feedback', { method: 'PUT', body: JSON.stringify({ babyId, recipeId, feedback }) });

export const clearFeedback = (babyId: string, recipeId: string) =>
  request<void>(`/api/foods/feedback?babyId=${babyId}&recipeId=${recipeId}`, { method: 'DELETE' });
