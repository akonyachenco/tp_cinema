import{TicketDto} from '../../shared/models'
export interface SeatDto {
  seatId: number;
  rowNumber: number;
  seatNumber: number;
  seatType: string; // 'standard', 'vip', 'blocked'
  priceMultiplier: number;
  hallId: number;
  basePrice?: number; // Может приходить с залом
  hallName?: string;
  ticketList?: TicketDto[];
  status: string; // Обычно не нужно
}

// Для компонента выбора мест
export interface SeatSelection {
  seatId: number;
  rowNumber: number;
  seatNumber: number;
  seatType: string;
  price: number; // basePrice * priceMultiplier
  isSelected: boolean;
  isBooked: boolean;
}

// Запрос на бронирование мест
export interface BookSeatsRequest {
  sessionId: number;
  seatIds: number[];
}