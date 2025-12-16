// src/app/core/services/hall.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { HallDto } from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class HallService {
  constructor(private api: ApiService) {}

  // Получить все залы
  getAllHalls(): Observable<HallDto[]> {
    return this.api.get<HallDto[]>('halls');
  }

  // Получить зал по ID
  getHallById(id: number): Observable<HallDto> {
    return this.api.get<HallDto>(`halls/${id}`);
  }

  // Получить доступные залы
  getAvailableHalls(): Observable<HallDto[]> {
    return this.api.get<HallDto[]>('halls/available');
  }

  // Создать зал (админ)
  createHall(hall: HallDto): Observable<HallDto> {
    return this.api.post<HallDto>('halls', hall);
  }

  // Обновить зал (админ)
  updateHall(id: number, hall: HallDto): Observable<HallDto> {
    return this.api.put<HallDto>(`halls/${id}`, hall);
  }

  // Удалить зал (админ)
  deleteHall(id: number): Observable<void> {
    return this.api.delete<void>(`halls/${id}`);
  }

  // Получить схему мест зала
  getHallLayout(hallId: number): Observable<any> {
    return this.api.get<any>(`halls/${hallId}/layout`);
  }
}