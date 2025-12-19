import {SessionDto, SeatDto} from '../../shared/models'
export interface HallDto {
  hallId: number;
  status: string;
  basePrice: number; // Было BigDecimal
  rowsCount: number; // Лучше переименовать в rowsCount
  seatsPerRow: number;
  hallName: string;
  hallType: string;
  sessionList?: SessionDto[]; // Опционально
  seatList?: SeatDto[]; // Опционально
}

// Упрощённая версия для выпадающего списка
export interface HallOption {
  hallId: number;
  hallName: string;
  capacity: number; // rows_count * seatsPerRow
  hallType: string;
}

// Для конструктора залов
export interface HallLayout {
  hallId: number;
  hallName: string;
  rows: SeatRow[];
}

export interface SeatRow {
  rowNumber: number;
  seats: SeatDto[];
}