// src/app/shared/models/session.model.ts
import { BookingDto, HallDto, FilmDto } from '../../shared/models';
export interface SessionDto {
  sessionId: number;
  status: string;
  dateTime: string; // Было LocalDateTime
  filmId: number;
  hallId: number;
  bookingList?: BookingDto[]; // Обычно не нужно на фронте
}

// Расширенная версия для страницы выбора сеанса
export interface SessionDetails extends SessionDto {
  film?: FilmDto; // Детали фильма
  hall?: HallDto; // Информация о зале
  availableSeats?: number; // Количество свободных мест
  basePrice?: number; // Базовая цена из зала
}

// Для формы создания/редактирования сеанса
export interface CreateSessionRequest {
  filmId: number;
  hallId: number;
  dateTime: string;
  status?: string; // По умолчанию "active"
}