// src/app/shared/models/ticket.model.ts
export interface TicketDto {
  ticketId: number;
  creationDate: string; // Было LocalDateTime
  price: number; // Было BigDecimal
  ticketCode: string;
  seatId: number;
  bookingId: number;
}

// Для электронного билета
export interface ElectronicTicket extends TicketDto {
  filmTitle?: string;
  sessionDateTime?: string;
  hallName?: string;
  seatNumber?: string; // "Ряд 3, Место 12"
  userName?: string;
  qrCodeUrl?: string; // URL для QR-кода
}