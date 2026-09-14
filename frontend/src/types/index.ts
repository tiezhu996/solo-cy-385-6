export interface Baby {
  id: number;
  name: string;
  birthday: string;
  bloodType?: string;
  initialHeight?: number;
  initialWeight?: number;
}

export type FeedbackType = 'like' | 'neutral' | 'dislike';

export interface FoodRecipe {
  id: number;
  monthAgeMin: number;
  monthAgeMax: number;
  name: string;
  ingredients?: string;
  steps?: string;
  nutrition?: string;
  allergens?: string;
  /** 当前宝宝的反馈，未标记为 null/undefined */
  feedback?: FeedbackType | null;
}
