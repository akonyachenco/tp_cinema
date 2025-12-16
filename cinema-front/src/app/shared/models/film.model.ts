import {SessionDto} from '../../shared/models'
export interface FilmDto {
  filmId: number;
  description: string;
  title: string;
  releaseDate: string;
  posterUrl?: string;
  trailerUrl?: string;
  duration: number; // Было Short
  directorId: number;
  countryId: number;
  ageRating: string;
  genres: string[];
  sessionList?: SessionDto[]; // Опционально, не всегда нужны все сеансы
}

// Упрощённая версия для списка фильмов (афиши)
export interface FilmListItem {
  filmId: number;
  title: string;
  duration: number;
  ageRating: string;
  posterUrl?: string;
  genres: string[];
  // Сеансы на ближайшие дни
  upcomingSessions?: SessionTimeSlot[];
}

export interface SessionTimeSlot {
  sessionId: number;
  dateTime: string;
  hallName: string;
  price: number;
}