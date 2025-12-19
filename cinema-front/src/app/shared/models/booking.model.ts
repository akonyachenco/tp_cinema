import { TicketDto, SessionDetails, SeatSelection, UserDto } from '../../shared/models';

export interface BookingDto {
  bookingId: number;
  bookingTime: string;
  totalCost: number;
  userId: number;
  sessionId: number;
  status: string;
  ticketList?: TicketDto[];
  canCancel?: boolean;
}

export interface BookingDetails extends BookingDto {
  filmTitle?: string;
  sessionDateTime?: string;
  hallName?: string;
  seatNumbers?: string[];
  canCancel?: boolean;
}

export interface BookingSummary {
  session: SessionDetails;
  selectedSeats: SeatSelection[];
  totalPrice: number;
  user?: UserDto;
}