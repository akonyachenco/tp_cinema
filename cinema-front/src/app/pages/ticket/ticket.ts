import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BookingService } from '../../core/services/booking.service';
import { TicketService } from '../../core/services/ticket.service';
import { MovieService } from '../../core/services/movie.service';
import { SessionService } from '../../core/services/session.service';
import { HallService } from '../../core/services/hall.service';
import { AuthService } from '../../core/services/auth.service';
import { TicketDto, BookingDto, SessionDto, FilmDto, HallDto, SeatDto, UserDto, ElectronicTicket } from '../../shared/models';

@Component({
  selector: 'app-ticket',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './ticket.html',
  styleUrls: ['./ticket.css']
})
export class TicketComponent implements OnInit {
  tickets: TicketDto[] = [];
  currentTicketIndex: number = 0;
  booking: BookingDto | null = null;
  session: SessionDto | null = null;
  movie: FilmDto | null = null;
  hall: HallDto | null = null;
  user: UserDto | null = null;
  isLoading = true;
  errorMessage = '';
  isPrintMode = false;
  seatInfo: Map<number, { row: number, number: number, type: string }> = new Map();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    private ticketService: TicketService,
    private movieService: MovieService,
    private sessionService: SessionService,
    private hallService: HallService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const ticketId = +params['id'];
      const bookingId = params['bookingId'] ? +params['bookingId'] : null;
      
      if (ticketId) {
        this.loadTicket(ticketId);
      } else if (bookingId) {
        this.loadBooking(bookingId);
      } else {
        this.showDemoMessage();
      }
    });
  }

  get currentTicket(): TicketDto | null {
    return this.tickets.length > 0 ? this.tickets[this.currentTicketIndex] : null;
  }

  loadTicket(ticketId: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.ticketService.getTicketById(ticketId).subscribe({
      next: (ticket) => {
        this.tickets = [ticket];
        this.loadBooking(ticket.bookingId);
      },
      error: (error) => {
        console.error('Ошибка загрузки билета:', error);
        this.errorMessage = 'Билет не найден';
        this.isLoading = false;
      }
    });
  }

  loadBooking(bookingId: number): void {
    this.bookingService.getBookingById(bookingId).subscribe({
      next: (booking) => {
        this.booking = booking;
        
        // Загружаем все билеты бронирования
        if (booking.ticketList && booking.ticketList.length > 0) {
          this.tickets = booking.ticketList;
          this.currentTicketIndex = 0;
          this.loadSeatInfoForTickets();
        } else {
          // Если билеты не пришли с бронированием, загружаем отдельно
          this.loadTicketsByBooking(bookingId);
        }
        
        // Загружаем дополнительную информацию
        if (booking.sessionId) {
          this.loadSession(booking.sessionId);
        }
        
        // Загружаем информацию о пользователе
        this.user = this.authService.getCurrentUser();
        
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка загрузки бронирования:', error);
        this.errorMessage = 'Бронирование не найдено';
        this.isLoading = false;
      }
    });
  }

  loadTicketsByBooking(bookingId: number): void {
    this.ticketService.getTicketsByBooking(bookingId).subscribe({
      next: (tickets) => {
        this.tickets = tickets;
        if (tickets.length > 0) {
          this.loadSeatInfoForTickets();
        }
      },
      error: (error) => {
        console.error('Ошибка загрузки билетов бронирования:', error);
      }
    });
  }

  loadSession(sessionId: number): void {
    this.sessionService.getSessionById(sessionId).subscribe({
      next: (session) => {
        this.session = session;
        
        // Загружаем информацию о фильме
        if (session.filmId) {
          this.loadMovie(session.filmId);
        }
        
        // Загружаем информацию о зале
        if (session.hallId) {
          this.loadHall(session.hallId);
        }
      },
      error: (error) => {
        console.error('Ошибка загрузки сеанса:', error);
      }
    });
  }

  loadMovie(movieId: number): void {
    this.movieService.getMovieById(movieId).subscribe({
      next: (movie) => {
        this.movie = movie || null;
      },
      error: (error) => {
        console.error('Ошибка загрузки фильма:', error);
      }
    });
  }

  loadHall(hallId: number): void {
    this.hallService.getHallById(hallId).subscribe({
      next: (hall) => {
        this.hall = hall;
        // После загрузки зала можем загрузить информацию о местах
        if (hall.seatList && hall.seatList.length > 0) {
          this.processSeatInfoFromHall(hall.seatList);
        }
      },
      error: (error) => {
        console.error('Ошибка загрузки зала:', error);
      }
    });
  }

  loadSeatInfoForTickets(): void {
    // Здесь должен быть API вызов для получения информации о местах
    // Пока используем заглушку для каждого билета
    this.tickets.forEach(ticket => {
      if (!this.seatInfo.has(ticket.seatId)) {
        this.seatInfo.set(ticket.seatId, {
          row: Math.floor(ticket.seatId / 10) + 1,
          number: ticket.seatId % 10 || 10,
          type: ticket.seatId % 3 === 0 ? 'VIP' : 'Стандарт'
        });
      }
    });
  }

  processSeatInfoFromHall(seats: SeatDto[]): void {
    seats.forEach(seat => {
      this.seatInfo.set(seat.seatId, {
        row: seat.rowNumber,
        number: seat.seatNumber,
        type: seat.seatType === 'vip' ? 'VIP' : 
               seat.seatType === 'blocked' ? 'Заблокировано' : 'Стандарт'
      });
    });
  }

  showDemoMessage(): void {
    this.isLoading = false;
    // Показываем демо-сообщение из HTML
  }

  // Методы для отображения данных
  getSeatInfo(seatId: number): { row: number, number: number, type: string } {
    const info = this.seatInfo.get(seatId);
    return info || { 
      row: Math.floor(seatId / 10) + 1, 
      number: seatId % 10 || 10, 
      type: 'Стандарт' 
    };
  }

  getSessionInfo(): any {
    if (!this.session || !this.movie || !this.hall) {
      return {
        filmTitle: 'Загрузка...',
        dateTime: new Date().toISOString(),
        hallName: 'Загрузка...',
        format: '2D'
      };
    }

    return {
      filmTitle: this.movie.title,
      dateTime: this.session.dateTime,
      hallName: this.hall.hallName,
      format: this.hall.hallType
    };
  }

  getUserInfo(): any {
    if (!this.user && this.booking) {
      // Если пользователь не загружен из authService, но есть booking
      return {
        name: 'Гость',
        surname: '',
        email: 'email@example.com'
      };
    }

    if (!this.user) {
      return {
        name: 'Гость',
        surname: '',
        email: 'email@example.com'
      };
    }

    return {
      name: this.user.name,
      surname: this.user.surname,
      email: this.user.email
    };
  }

  formatDateTime(dateTime: string): string {
    try {
      const date = new Date(dateTime);
      return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return 'Дата не определена';
    }
  }

  getQrCodeUrl(): string {
    if (!this.currentTicket || !this.movie || !this.session) {
      return 'https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=Загрузка...';
    }
    
    const ticketData = `Кинотеатр\nБилет: ${this.currentTicket.ticketCode}\nФильм: ${this.movie.title}\nСеанс: ${this.formatDateTime(this.session.dateTime)}\nМесто: Ряд ${this.getSeatInfo(this.currentTicket.seatId).row}, Место ${this.getSeatInfo(this.currentTicket.seatId).number}`;
    
    return `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(ticketData)}`;
  }

  // Навигация между билетами
  selectTicket(ticket: TicketDto): void {
    const index = this.tickets.findIndex(t => t.ticketId === ticket.ticketId);
    if (index !== -1) {
      this.currentTicketIndex = index;
    }
  }

  selectTicketById(ticketId: number): void {
    const index = this.tickets.findIndex(t => t.ticketId === ticketId);
    if (index !== -1) {
      this.currentTicketIndex = index;
    }
  }

  navigateToTicket(ticketId: number): void {
    // Переходим на страницу билета по его ID
    this.router.navigate(['/ticket', ticketId]);
  }

  // Действия с билетом
  printTicket(): void {
    this.isPrintMode = true;
    setTimeout(() => {
      window.print();
      this.isPrintMode = false;
    }, 100);
  }

  downloadPdf(): void {
    if (!this.currentTicket) return;
    
    this.ticketService.generateTicketPdf(this.currentTicket.ticketId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `билет-${this.currentTicket!.ticketCode}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Ошибка генерации PDF:', error);
        alert('Не удалось сгенерировать PDF. Пожалуйста, попробуйте распечатать билет.');
      }
    });
  }

  goToAccount(): void {
    this.router.navigate(['/account']);
  }

  getAllTickets(): TicketDto[] {
    return this.tickets;
  }

  canCancel(): boolean {
    if (!this.booking || this.booking.status !== 'active' || !this.session) return false;
    
    try {
      const sessionTime = new Date(this.session.dateTime);
      const now = new Date();
      const hoursDiff = (sessionTime.getTime() - now.getTime()) / (1000 * 60 * 60);
      
      return hoursDiff > 1;
    } catch (error) {
      return false;
    }
  }

  cancelBooking(): void {
    if (!this.booking) return;
    
    if (confirm('Вы уверены, что хотите отменить бронирование? Средства будут возвращены на карту в течение 3-5 дней.')) {
      this.bookingService.cancelBooking(this.booking.bookingId).subscribe({
        next: () => {
          if (this.booking) {
            this.booking.status = 'cancelled';
          }
          alert('Бронирование успешно отменено!');
        },
        error: (error) => {
          console.error('Ошибка отмены бронирования:', error);
          alert('Ошибка при отмене бронирования. Пожалуйста, попробуйте позже.');
        }
      });
    }
  }

  // Дополнительные методы для обработки времени
  formatTime(dateTime: string): string {
    try {
      const date = new Date(dateTime);
      return date.toLocaleTimeString('ru-RU', {
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return '--:--';
    }
  }

  formatDate(dateTime: string): string {
    try {
      const date = new Date(dateTime);
      return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      });
    } catch (error) {
      return 'Дата не определена';
    }
  }
}
