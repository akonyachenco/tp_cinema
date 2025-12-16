// pages/confirm/confirm.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

interface BookingConfirmation {
  bookingId: number;
  bookingTime: Date;
  totalPrice: number;
  session: {
    dateTime: Date;
    hallName: string;
    hallType: string;
  };
  movie: {
    title: string;
    duration: number;
    ageRating: string;
  };
  seats: Array<{
    row: number;
    seat: number;
    type: string;
    price: number;
  }>;
}

@Component({
  selector: 'app-confirm',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './confirm.html',
  styleUrls: ['./confirm.css']
})
export class ConfirmComponent implements OnInit {
  booking!: BookingConfirmation;
  isLoading = true;
  
  // Мок данные для демо (замените данными из state)
  private mockBooking: BookingConfirmation = {
    bookingId: Math.floor(Math.random() * 9000) + 1000,
    bookingTime: new Date(),
    totalPrice: 1500,
    session: {
      dateTime: new Date(Date.now() + 86400000), // Завтра
      hallName: 'Большой зал (IMAX)',
      hallType: 'IMAX'
    },
    movie: {
      title: 'Дюна: Часть вторая',
      duration: 166,
      ageRating: '16+'
    },
    seats: [
      { row: 3, seat: 5, type: 'standard', price: 350 },
      { row: 3, seat: 6, type: 'standard', price: 350 },
      { row: 3, seat: 7, type: 'standard', price: 350 },
      { row: 4, seat: 4, type: 'vip', price: 525 },
      { row: 4, seat: 5, type: 'vip', price: 525 }
    ]
  };

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Получаем данные из state навигации
    const navigation = this.router.getCurrentNavigation();
    
    if (navigation?.extras.state) {
      const state = navigation.extras.state as any;
      
      this.booking = {
        bookingId: state.booking?.bookingId || this.mockBooking.bookingId,
        bookingTime: new Date(state.booking?.bookingTime || Date.now()),
        totalPrice: state.totalPrice || this.mockBooking.totalPrice,
        session: {
          dateTime: new Date(state.session?.dateTime || this.mockBooking.session.dateTime),
          hallName: state.hall?.hallName || this.mockBooking.session.hallName,
          hallType: state.hall?.hallType || this.mockBooking.session.hallType
        },
        movie: {
          title: state.movie?.title || this.mockBooking.movie.title,
          duration: state.movie?.duration || this.mockBooking.movie.duration,
          ageRating: state.movie?.ageRating || this.mockBooking.movie.ageRating
        },
        seats: (state.selectedSeats || this.mockBooking.seats).map((seat: any) => ({
          row: seat.rowNumber || seat.row,
          seat: seat.seatNumber || seat.seat,
          type: seat.seatType || seat.type,
          price: seat.price || (seat.basePrice || 350) * (seat.priceMultiplier || 1)
        }))
      };
    } else {
      // Используем мок если state пустой
      this.booking = this.mockBooking;
    }
    
    this.isLoading = false;
  }

  getFormattedDate(date: Date): string {
    return date.toLocaleDateString('ru-RU', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  getFormattedTime(date: Date): string {
    return date.toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSeatsText(): string {
    const seatsByRow: {[key: number]: number[]} = {};
    
    this.booking.seats.forEach(s => {
      if (!seatsByRow[s.row]) seatsByRow[s.row] = [];
      seatsByRow[s.row].push(s.seat);
    });
    
    return Object.keys(seatsByRow)
      .map(row => `Ряд ${row}: ${seatsByRow[+row].join(', ')}`)
      .join('; ');
  }

  printTicket(): void {
    window.print();
  }

  goToAccount(): void {
    this.router.navigate(['/account']);
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  downloadTicket(): void {
    // В реальном приложении здесь будет запрос на генерацию PDF
    alert('PDF билета будет скачан. В реальном приложении здесь будет запрос к бэкенду.');
  }

  getVatAmount(): number {
    return this.booking.totalPrice * 0.2; // 20% НДС
  }

  getTotalWithoutVat(): number {
    return this.booking.totalPrice - this.getVatAmount();
  }
}