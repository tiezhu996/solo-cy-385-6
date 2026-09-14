export interface Baby {
  /** 后端雪花 ID 以字符串返回，避免 JS Number 精度丢失 */
  id: string;
  name: string;
  birthday: string;
  bloodType?: string;
  initialHeight?: number;
  initialWeight?: number;
}

export type FeedbackType = 'like' | 'neutral' | 'dislike';

export interface FoodRecipe {
  id: string;
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
