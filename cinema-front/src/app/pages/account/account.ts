// src/app/pages/account/account.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { BookingService } from '../../core/services/booking.service';
import { UserDto, BookingDto } from '../../shared/models';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './account.html',
  styleUrls: ['./account.css'] // создайте этот файл
})
export class AccountComponent implements OnInit {
  user: UserDto | null = null;
  bookings: BookingDto[] = [];
  isLoading = true;
  activeTab: 'profile' | 'bookings' | 'settings' = 'bookings';

  constructor(
    private auth: AuthService,
    private bookingService: BookingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserData();
  }

  loadUserData(): void {
    this.user = this.auth.getCurrentUser();
    
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadBookings();
  }

  loadBookings(): void {
    this.isLoading = true;
    
    // Временные мок-данные бронирований
    const mockBookings: BookingDto[] = [
      {
        bookingId: 1,
        bookingTime: '2024-10-18T14:30:00',
        totalCost: 1200,
        userId: 1,
        sessionId: 101,
        status: 'active',
        ticketList: [
          {
            ticketId: 1,
            creationDate: '2024-10-18T14:30:00',
            price: 600,
            ticketCode: 'TK-2024-001-456789',
            seatId: 12,
            bookingId: 1
          },
          {
            ticketId: 2,
            creationDate: '2024-10-18T14:30:00',
            price: 600,
            ticketCode: 'TK-2024-001-456790',
            seatId: 13,
            bookingId: 1
          }
        ]
      },
      {
        bookingId: 2,
        bookingTime: '2024-10-01T18:15:00',
        totalCost: 400,
        userId: 1,
        sessionId: 102,
        status: 'cancelled',
        ticketList: [
          {
            ticketId: 3,
            creationDate: '2024-10-01T18:15:00',
            price: 400,
            ticketCode: 'TK-2024-001-123456',
            seatId: 8,
            bookingId: 2
          }
        ]
      },
      {
        bookingId: 3,
        bookingTime: '2026-10-20T19:45:00',
        totalCost: 300,
        userId: 1,
        sessionId: 103,
        status: 'active',
        ticketList: [
          {
            ticketId: 4,
            creationDate: '2024-10-20T19:45:00',
            price: 300,
            ticketCode: 'TK-2024-001-789012',
            seatId: 5,
            bookingId: 3
          }
        ]
      }
    ];

    // Имитация загрузки с сервера
    setTimeout(() => {
      this.bookings = mockBookings;
      this.isLoading = false;
    }, 500);

    // Позже замените на реальный вызов:
    // this.bookingService.getUserBookings(this.user.userId).subscribe({
    //   next: (bookings) => {
    //     this.bookings = bookings;
    //     this.isLoading = false;
    //   },
    //   error: (error) => {
    //     console.error('Ошибка загрузки бронирований:', error);
    //     this.isLoading = false;
    //   }
    // });
  }

  cancelBooking(bookingId: number): void {
    if (confirm('Вы уверены, что хотите отменить бронирование?')) {
      this.bookingService.cancelBooking(bookingId).subscribe({
        next: () => {
          const booking = this.bookings.find(b => b.bookingId === bookingId);
          if (booking) {
            booking.status = 'cancelled';
          }
          alert('Бронирование успешно отменено!');
        },
        error: (error) => {
          console.error('Ошибка отмены бронирования:', error);
          alert('Не удалось отменить бронирование');
        }
      });
    }
  }

  canCancelBooking(booking: BookingDto): boolean {
    if (booking.status !== 'active') return false;
    
    const bookingTime = new Date(booking.bookingTime);
    const now = new Date();
    const hoursDiff = (bookingTime.getTime() - now.getTime()) / (1000 * 60 * 60);
    return hoursDiff > 1; // Можно отменить за 1 час до сеанса
  }

  getBookingStatusText(status: string): string {
    switch(status) {
      case 'active': return 'Активна';
      case 'cancelled': return 'Отменена';
      case 'completed': return 'Завершена';
      default: return status;
    }
  }

  getBookingStatusClass(status: string): string {
    switch(status) {
      case 'active': return 'status-active';
      case 'cancelled': return 'status-cancelled';
      case 'completed': return 'status-completed';
      default: return '';
    }
  }

  setActiveTab(tab: 'profile' | 'bookings' | 'settings'): void {
    this.activeTab = tab;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSeatNumbers(tickets: any[]): string {
    if (!tickets || tickets.length === 0) return '';
    return tickets.length === 1 ? '1 место' : `${tickets.length} места`;
  }

  getUserAge(birthDate?: string): number {
    if (!birthDate) return 0;
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    
    return age;
  }
}