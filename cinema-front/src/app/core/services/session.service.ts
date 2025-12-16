// src/app/core/services/session.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { SessionDto, CreateSessionRequest } from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  constructor(private api: ApiService) {}

  // Получить все сеансы
  getAllSessions(): Observable<SessionDto[]> {
    return this.api.get<SessionDto[]>('sessions');
  }

  // Получить сеанс по ID
  getSessionById(id: number): Observable<SessionDto> {
    return this.api.get<SessionDto>(`sessions/${id}`);
  }

  // Получить сеансы фильма
  getSessionsByMovie(movieId: number): Observable<SessionDto[]> {
    return this.api.get<SessionDto[]>(`sessions/movie/${movieId}`);
  }

  // Получить сеансы на дату
  getSessionsByDate(date: string): Observable<SessionDto[]> {
    return this.api.get<SessionDto[]>(`sessions/date/${date}`);
  }

  // Создать сеанс (админ)
  createSession(session: CreateSessionRequest): Observable<SessionDto> {
    return this.api.post<SessionDto>('sessions', session);
  }

  // Обновить сеанс (админ)
  updateSession(id: number, session: SessionDto): Observable<SessionDto> {
    return this.api.put<SessionDto>(`sessions/${id}`, session);
  }

  // Удалить сеанс (админ)
  deleteSession(id: number): Observable<void> {
    return this.api.delete<void>(`sessions/${id}`);
  }

  // Получить доступные сеансы для бронирования
  getAvailableSessions(): Observable<SessionDto[]> {
    return this.api.get<SessionDto[]>('sessions/available');
  }
}