// src/app/core/services/booking.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { BookingDto, BookSeatsRequest } from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  constructor(private api: ApiService) {}

  // Создать бронирование
  createBooking(bookingRequest: BookingDto): Observable<BookingDto> {
    return this.api.post<BookingDto>('bookings', bookingRequest);
  }

  // Получить бронирование по ID
  getBookingById(id: number): Observable<BookingDto> {
    return this.api.get<BookingDto>(`bookings/${id}`);
  }

  // Получить бронирования пользователя
  getUserBookings(userId: number): Observable<BookingDto[]> {
    return this.api.get<BookingDto[]>(`bookings/user/${userId}`);
  }

  // Отменить бронирование
  cancelBooking(bookingId: number): Observable<void> {
    return this.api.delete<void>(`bookings/${bookingId}`);
  }

  // Получить историю бронирований
  getBookingHistory(userId: number): Observable<BookingDto[]> {
    return this.api.get<BookingDto[]>(`bookings/user/${userId}/history`);
  }

  // Подтвердить бронирование (оплата)
  confirmBooking(bookingId: number): Observable<BookingDto> {
    return this.api.put<BookingDto>(`bookings/${bookingId}/confirm`, {});
  }
}