// src/app/pages/ticket/ticket.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BookingService } from '../../core/services/booking.service';
import { TicketService } from '../../core/services/ticket.service';
import { TicketDto, BookingDto } from '../../shared/models';

@Component({
  selector: 'app-ticket',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './ticket.html',
  styleUrls: ['./ticket.css']
})
export class TicketComponent implements OnInit {
  ticket: TicketDto | null = null;
  booking: BookingDto | null = null;
  isLoading = true;
  errorMessage = '';
  isPrintMode = false;

  // Мок-данные для отображения
  mockTicket: TicketDto = {
    ticketId: 1,
    creationDate: '2024-10-18T14:30:00',
    price: 600,
    ticketCode: 'TK-2024-001-456789',
    seatId: 12,
    bookingId: 1
  };

  mockBooking: BookingDto = {
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
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const ticketId = +params['id'];
      if (ticketId) {
        this.loadTicket(ticketId);
      } else {
        // Если перешли с bookingId, показываем первый билет
        const bookingId = +params['bookingId'];
        if (bookingId) {
          this.loadBooking(bookingId);
        } else {
          this.showDemoTicket();
        }
      }
    });
  }

  loadTicket(ticketId: number): void {
    this.isLoading = true;
    
    // Временные мок-данные (замените на реальный запрос)
    setTimeout(() => {
      this.ticket = this.mockTicket;
      this.booking = this.mockBooking;
      this.isLoading = false;
      
      // Позже замените на:
      // this.ticketService.getTicketById(ticketId).subscribe({
      //   next: (ticket) => {
      //     this.ticket = ticket;
      //     this.loadBooking(ticket.bookingId);
      //   },
      //   error: (error) => {
      //     this.errorMessage = 'Билет не найден';
      //     this.isLoading = false;
      //   }
      // });
    }, 500);
  }

  loadBooking(bookingId: number): void {
    // Временные мок-данные
    setTimeout(() => {
      this.booking = this.mockBooking;
      this.ticket = this.mockBooking.ticketList?.[0] || null;
      this.isLoading = false;
    }, 500);
    
    // Позже замените на:
    // this.bookingService.getBookingById(bookingId).subscribe({
    //   next: (booking) => {
    //     this.booking = booking;
    //     this.ticket = booking.ticketList?.[0] || null;
    //     this.isLoading = false;
    //   }
    // });
  }

  showDemoTicket(): void {
    this.ticket = this.mockTicket;
    this.booking = this.mockBooking;
    this.isLoading = false;
  }

  // Методы для отображения данных
  getSeatInfo(seatId: number): { row: number, number: number, type: string } {
    // В реальном приложении получаем из API
    const seats: { [key: number]: any } = {
      12: { row: 3, number: 12, type: 'VIP' },
      13: { row: 3, number: 13, type: 'VIP' },
      8: { row: 5, number: 8, type: 'Стандарт' },
      5: { row: 2, number: 5, type: 'Стандарт' }
    };
    
    return seats[seatId] || { row: 1, number: seatId, type: 'Стандарт' };
  }

  getSessionInfo(sessionId: number): any {
    // В реальном приложении получаем из API
    const sessions: { [key: number]: any } = {
      101: {
        filmTitle: 'Дюна: Часть вторая',
        dateTime: '2024-10-18T19:00:00',
        hallName: 'Большой зал (IMAX)',
        format: 'IMAX'
      },
      102: {
        filmTitle: 'Оппенгеймер',
        dateTime: '2024-10-01T20:00:00',
        hallName: 'Малый зал',
        format: '2D'
      },
      103: {
        filmTitle: 'Годзилла и Конг',
        dateTime: '2024-10-20T21:30:00',
        hallName: '4DX зал',
        format: '4DX'
      }
    };
    
    return sessions[sessionId] || {
      filmTitle: 'Неизвестный фильм',
      dateTime: new Date().toISOString(),
      hallName: 'Зал 1',
      format: '2D'
    };
  }

  getUserInfo(userId: number): any {
    return {
      name: 'Иван',
      surname: 'Иванов',
      email: 'user@example.com'
    };
  }

  formatDateTime(dateTime: string): string {
    const date = new Date(dateTime);
    return date.toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatTime(dateTime: string): string {
    const date = new Date(dateTime);
    return date.toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatDate(dateTime: string): string {
    const date = new Date(dateTime);
    return date.toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  getQrCodeUrl(): string {
    // Генерация QR-кода с данными билета
    const ticketData = this.ticket 
      ? `Кинотеатр\nБилет: ${this.ticket.ticketCode}\nФильм: ${this.getSessionInfo(this.booking?.sessionId || 101).filmTitle}\nСеанс: ${this.formatDateTime(this.getSessionInfo(this.booking?.sessionId || 101).dateTime)}`
      : 'Билет не загружен';
    
    // Используем сервис для генерации QR (например, qrcode.js)
    return `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(ticketData)}`;
  }

  printTicket(): void {
    this.isPrintMode = true;
    setTimeout(() => {
      window.print();
      this.isPrintMode = false;
    }, 100);
  }

  downloadPdf(): void {
    // Имитация скачивания PDF
    alert('PDF-версия билета будет сгенерирована и скачана');
    // this.ticketService.generateTicketPdf(this.ticket!.ticketId).subscribe(blob => {
    //   const url = window.URL.createObjectURL(blob);
    //   const a = document.createElement('a');
    //   a.href = url;
    //   a.download = `билет-${this.ticket!.ticketCode}.pdf`;
    //   a.click();
    // });
  }

  goToAccount(): void {
    this.router.navigate(['/account']);
  }

  goToHome(): void {
    this.router.navigate(['/']);
  }

  // Получить все билеты бронирования
  getAllTickets(): TicketDto[] {
    return this.booking?.ticketList || [];
  }

  // Проверка возможности отмены (за 1 час до сеанса)
  canCancel(): boolean {
    if (!this.booking || this.booking.status !== 'active') return false;
    
    const sessionTime = new Date(this.getSessionInfo(this.booking.sessionId).dateTime);
    const now = new Date();
    const hoursDiff = (sessionTime.getTime() - now.getTime()) / (1000 * 60 * 60);
    
    return hoursDiff > 1;
  }

  cancelBooking(): void {
    if (confirm('Вы уверены, что хотите отменить бронирование? Средства будут возвращены на карту в течение 3-5 дней.')) {
      if (this.booking) {
        this.bookingService.cancelBooking(this.booking.bookingId).subscribe({
          next: () => {
            if (this.booking) {
              this.booking.status = 'cancelled';
            }
            alert('Бронирование успешно отменено!');
          },
          error: (error) => {
            alert('Ошибка при отмене бронирования');
          }
        });
      }
    }
  }
}