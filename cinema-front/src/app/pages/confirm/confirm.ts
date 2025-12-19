// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { Router, RouterModule } from '@angular/router';

// interface BookingConfirmation {
//   bookingId: number;
//   bookingTime: Date;
//   totalPrice: number;
//   session: {
//     dateTime: Date;
//     hallName: string;
//     hallType: string;
//   };
//   movie: {
//     title: string;
//     duration: number;
//     ageRating: string;
//   };
//   seats: Array<{
//     row: number;
//     seat: number;
//     type: string;
//     price: number;
//   }>;
// }

// @Component({
//   selector: 'app-confirm',
//   standalone: true,
//   imports: [CommonModule, RouterModule],
//   templateUrl: './confirm.html',
//   styleUrls: ['./confirm.css']
// })
// export class ConfirmComponent implements OnInit {
//   booking!: BookingConfirmation;
//   isLoading = true;
  
//   // Мок данные для демо (замените данными из state)
//   private mockBooking: BookingConfirmation = {
//     bookingId: Math.floor(Math.random() * 9000) + 1000,
//     bookingTime: new Date(),
//     totalPrice: 1500,
//     session: {
//       dateTime: new Date(Date.now() + 86400000), // Завтра
//       hallName: 'Большой зал (IMAX)',
//       hallType: 'IMAX'
//     },
//     movie: {
//       title: 'Дюна: Часть вторая',
//       duration: 166,
//       ageRating: '16+'
//     },
//     seats: [
//       { row: 3, seat: 5, type: 'standard', price: 350 },
//       { row: 3, seat: 6, type: 'standard', price: 350 },
//       { row: 3, seat: 7, type: 'standard', price: 350 },
//       { row: 4, seat: 4, type: 'vip', price: 525 },
//       { row: 4, seat: 5, type: 'vip', price: 525 }
//     ]
//   };

//   constructor(private router: Router) {}

//   ngOnInit(): void {

//     const navigation = this.router.getCurrentNavigation();
    
//     if (navigation?.extras.state) {
//       const state = navigation.extras.state as any;
      
//       this.booking = {
//         bookingId: state.booking?.bookingId || this.mockBooking.bookingId,
//         bookingTime: new Date(state.booking?.bookingTime || Date.now()),
//         totalPrice: state.totalPrice || this.mockBooking.totalPrice,
//         session: {
//           dateTime: new Date(state.session?.dateTime || this.mockBooking.session.dateTime),
//           hallName: state.hall?.hallName || this.mockBooking.session.hallName,
//           hallType: state.hall?.hallType || this.mockBooking.session.hallType
//         },
//         movie: {
//           title: state.movie?.title || this.mockBooking.movie.title,
//           duration: state.movie?.duration || this.mockBooking.movie.duration,
//           ageRating: state.movie?.ageRating || this.mockBooking.movie.ageRating
//         },
//         seats: (state.selectedSeats || this.mockBooking.seats).map((seat: any) => ({
//           row: seat.rowNumber || seat.row,
//           seat: seat.seatNumber || seat.seat,
//           type: seat.seatType || seat.type,
//           price: seat.price || (seat.basePrice || 350) * (seat.priceMultiplier || 1)
//         }))
//       };
//     } else {
//       this.booking = this.mockBooking;
//     }
    
//     this.isLoading = false;
//   }

//   getFormattedDate(date: Date): string {
//     return date.toLocaleDateString('ru-RU', {
//       weekday: 'long',
//       day: 'numeric',
//       month: 'long',
//       year: 'numeric'
//     });
//   }

//   getFormattedTime(date: Date): string {
//     return date.toLocaleTimeString('ru-RU', {
//       hour: '2-digit',
//       minute: '2-digit'
//     });
//   }

//   getSeatsText(): string {
//     const seatsByRow: {[key: number]: number[]} = {};
    
//     this.booking.seats.forEach(s => {
//       if (!seatsByRow[s.row]) seatsByRow[s.row] = [];
//       seatsByRow[s.row].push(s.seat);
//     });
    
//     return Object.keys(seatsByRow)
//       .map(row => `Ряд ${row}: ${seatsByRow[+row].join(', ')}`)
//       .join('; ');
//   }

//   printTicket(): void {
//     window.print();
//   }

//   goToAccount(): void {
//     this.router.navigate(['/account']);
//   }

//   goHome(): void {
//     this.router.navigate(['/']);
//   }

//   downloadTicket(): void {
//     // В реальном приложении здесь будет запрос на генерацию PDF
//     alert('PDF билета будет скачан. В реальном приложении здесь будет запрос к бэкенду.');
//   }

//   getVatAmount(): number {
//     return this.booking.totalPrice * 0.2; // 20% НДС
//   }

//   getTotalWithoutVat(): number {
//     return this.booking.totalPrice - this.getVatAmount();
//   }
// }

// pages/confirm/confirm.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { BookingService } from '../../core/services/booking.service';
import { TicketService } from '../../core/services/ticket.service';
import { SessionService } from '../../core/services/session.service';
import { MovieService } from '../../core/services/movie.service';
import { HallService } from '../../core/services/hall.service';
import { BookingDto, TicketDto, SessionDto, FilmDto, HallDto } from '../../shared/models';

interface BookingSeat {
  row: number;
  seat: number;
  type: string;
  price: number;
}

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
  seats: BookingSeat[];
}

@Component({
  selector: 'app-confirm',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './confirm.html',
  styleUrls: ['./confirm.css']
})
export class ConfirmComponent implements OnInit, OnDestroy {
  booking!: BookingConfirmation;
  isLoading = true;
  errorMessage: string | null = null;
  
  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private bookingService: BookingService,
    private ticketService: TicketService,
    private sessionService: SessionService,
    private movieService: MovieService,
    private hallService: HallService
  ) {}

  ngOnInit(): void {
    // Получаем bookingId из параметров маршрута или state
    this.route.params.subscribe(params => {
      const bookingId = params['id'];
      if (bookingId) {
        this.loadBookingDetails(+bookingId);
      } else {
        this.tryGetBookingFromState();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private tryGetBookingFromState(): void {
    // Проверяем, есть ли данные в state
    const navigation = this.router.getCurrentNavigation();
    
    if (navigation?.extras.state?.['bookingId']) {
      const bookingId = navigation.extras.state['bookingId'];
      this.loadBookingDetails(bookingId);
    } else if (navigation?.extras.state?.['bookingRequest']) {
      // Если только что создали бронирование, но еще нет ID
      this.createBookingFromRequest(navigation.extras.state['bookingRequest']);
    } else {
      this.errorMessage = 'Не найдены данные бронирования';
      this.isLoading = false;
    }
  }

  private loadBookingDetails(bookingId: number): void {
    this.isLoading = true;
    this.errorMessage = null;
    
    this.bookingService.getBookingById(bookingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: async (bookingData: BookingDto) => {
          await this.loadFullBookingData(bookingData);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Ошибка загрузки бронирования:', error);
          this.errorMessage = 'Не удалось загрузить данные бронирования';
          this.isLoading = false;
        }
      });
  }

  private createBookingFromRequest(bookingRequest: any): void {
    this.isLoading = true;
    
    this.bookingService.createBooking(bookingRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: async (bookingData: BookingDto) => {
          // Сохраняем ID в URL без перезагрузки страницы
          this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { bookingId: bookingData.bookingId },
            replaceUrl: true
          });
          
          await this.loadFullBookingData(bookingData);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Ошибка создания бронирования:', error);
          this.errorMessage = 'Не удалось создать бронирование';
          this.isLoading = false;
          
          // Перенаправляем обратно на выбор мест
          setTimeout(() => {
            this.router.navigate(['/booking', bookingRequest.sessionId]);
          }, 3000);
        }
      });
  }

  private async loadFullBookingData(bookingData: BookingDto): Promise<void> {
    try {
      // Загружаем детали сеанса
      const session = await this.loadSession(bookingData.sessionId);
      
      // Загружаем детали фильма
      const movie = session ? await this.loadMovie(session.filmId) : null;
      
      // Загружаем детали зала
      const hall = session ? await this.loadHall(session.hallId) : null;
      
      // Загружаем билеты (чтобы получить места)
      const tickets = await this.loadTickets(bookingData.bookingId);
      
      // Преобразуем в формат для отображения
      this.booking = {
        bookingId: bookingData.bookingId,
        bookingTime: new Date(bookingData.bookingTime),
        totalPrice: bookingData.totalCost,
        session: {
          dateTime: session ? new Date(session.dateTime) : new Date(),
          hallName: hall?.hallName || 'Неизвестный зал',
          hallType: hall?.hallType || 'standard'
        },
        movie: {
          title: movie?.title || 'Неизвестный фильм',
          duration: movie?.duration || 0,
          ageRating: movie?.ageRating || '0+'
        },
        seats: await this.getSeatsFromTickets(tickets)
      };
    } catch (error) {
      console.error('Ошибка загрузки дополнительных данных:', error);
      throw error;
    }
  }

  private loadSession(sessionId: number): Promise<SessionDto> {
    return new Promise((resolve, reject) => {
      this.sessionService.getSessionById(sessionId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: resolve,
          error: reject
        });
    });
  }

  private loadMovie(filmId: number): Promise<FilmDto> {
    return new Promise((resolve, reject) => {
      this.movieService.getMovieById(filmId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (movie) => {
            if (movie) {
              resolve(movie);
            } else {
              reject(new Error('Фильм не найден'));
            }
          },
          error: reject
        });
    });
  }

  private loadHall(hallId: number): Promise<HallDto> {
    return new Promise((resolve, reject) => {
      this.hallService.getHallById(hallId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: resolve,
          error: reject
        });
    });
  }

  private loadTickets(bookingId: number): Promise<TicketDto[]> {
    return new Promise((resolve, reject) => {
      this.ticketService.getTicketsByBooking(bookingId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: resolve,
          error: reject
        });
    });
  }

  private async getSeatsFromTickets(tickets: TicketDto[]): Promise<BookingSeat[]> {
    // В реальном приложении здесь нужно загрузить информацию о местах
    // по seatId из каждого билета. Пока используем базовые данные.
    
    return tickets.map((ticket, index) => ({
      row: Math.floor(index / 10) + 1, // Временная логика
      seat: (index % 10) + 1,
      type: 'standard',
      price: ticket.price
    }));
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
    if (!this.booking?.seats) return '';
    
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
    if (!this.booking) return;
    
    this.ticketService.generateTicketPdf(this.booking.bookingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          // Создаем ссылку для скачивания
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `ticket-${this.booking.bookingId}.pdf`;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: (error) => {
          console.error('Ошибка скачивания билета:', error);
          alert('Не удалось скачать билет. Попробуйте позже.');
        }
      });
  }

  getVatAmount(): number {
    return this.booking?.totalPrice ? this.booking.totalPrice * 0.2 : 0; // 20% НДС
  }

  getTotalWithoutVat(): number {
    return this.booking?.totalPrice ? this.booking.totalPrice - this.getVatAmount() : 0;
  }
  // Добавьте в класс ConfirmComponent
getSeatsCountText(count: number): string {
  if (count % 10 === 1 && count % 100 !== 11) return 'место';
  if (count % 10 >= 2 && count % 10 <= 4 && 
      (count % 100 < 10 || count % 100 >= 20)) {
    return 'места';
  }
  return 'мест';
}
}