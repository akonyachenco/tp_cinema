// src/app/core/services/ticket.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { TicketDto } from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  constructor(private api: ApiService) {}

  // Получить билет по ID
  getTicketById(id: number): Observable<TicketDto> {
    return this.api.get<TicketDto>(`tickets/${id}`);
  }

  // Получить билеты бронирования
  getTicketsByBooking(bookingId: number): Observable<TicketDto[]> {
    return this.api.get<TicketDto[]>(`tickets/booking/${bookingId}`);
  }

  // Сгенерировать PDF билета
  generateTicketPdf(ticketId: number): Observable<Blob> {
    return this.api.get<Blob>(`tickets/${ticketId}/pdf`, {
      responseType: 'blob' as 'json'
    });
  }

  // Получить QR код билета
  getTicketQrCode(ticketId: number): Observable<string> {
    return this.api.get<string>(`tickets/${ticketId}/qrcode`);
  }
}