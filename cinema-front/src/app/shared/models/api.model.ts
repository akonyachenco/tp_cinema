// src/app/shared/models/api.model.ts
// Общий ответ API
export interface ApiResponse<T> {
  data: T;
  message?: string;
  timestamp: string;
  status: number;
}

// Пагинированный ответ
export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// Ошибка API
export interface ApiError {
  status: number;
  message: string;
  errors?: Record<string, string[]>;
  timestamp: string;
  path: string;
}

// Параметры запроса
export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}