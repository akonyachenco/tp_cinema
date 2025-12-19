// pages/session/session.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { HallService } from '../../core/services/hall.service';
import { MovieService } from '../../core/services/movie.service';
import { BookingService } from '../../core/services/booking.service';
import { TicketService } from '../../core/services/ticket.service';
import { AuthService } from '../../core/services/auth.service';
import { 
  SessionDto, 
  HallDto, 
  FilmDto, 
  SeatDto, 
  BookingDto,
  TicketDto
} from '../../shared/models';

interface SeatUI {
  seat: SeatDto;
  isSelected: boolean;
  isBooked: boolean;
}

@Component({
  selector: 'app-session',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './session.html',
  styleUrls: ['./session.css']
})
export class SessionComponent implements OnInit {
  sessionId!: number;
  session: SessionDto | null = null;
  hall: HallDto | null = null;
  movie: FilmDto | null = null;
  seats: SeatUI[] = [];
  selectedSeats: SeatDto[] = [];
  
  isLoading = true;
  isBooking = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: SessionService,
    private hallService: HallService,
    private movieService: MovieService,
    private bookingService: BookingService,
    private ticketService: TicketService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.sessionId = +params['id'];
      this.loadSessionData();
    });
  }

  loadSessionData(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    // Загружаем сеанс со всеми бронированиями
    this.sessionService.getSessionById(this.sessionId).subscribe({
      next: (session) => {
        this.session = session;
        
        // Загружаем зал
        this.hallService.getHallById(session.hallId).subscribe({
          next: (hall) => {
            this.hall = hall;
            
            // Загружаем фильм
            this.movieService.getMovieById(session.filmId).subscribe({
              next: (movie) => {
                this.movie = movie || null;
                
                if (!this.movie) {
                  console.warn('Фильм не найден');
                  this.errorMessage = 'Информация о фильме не найдена';
                  this.isLoading = false;
                  return;
                }
                
                // Загружаем схему мест зала и забронированные места
                this.loadHallLayout();
              },
              error: (error) => {
                this.handleError('Ошибка загрузки фильма', error);
                this.movie = null;
              }
            });
          },
          error: (error) => {
            this.handleError('Ошибка загрузки зала', error);
          }
        });
      },
      error: (error) => {
        this.handleError('Ошибка загрузки сеанса', error);
      }
    });
  }

  loadHallLayout(): void {
    if (!this.hall || !this.session) return;
    
    this.hallService.getHallLayout(this.hall.hallId).subscribe({
      next: (response: any) => {
        // Проверяем разные форматы ответа
        let seatsFromApi: SeatDto[] = [];
        
        if (Array.isArray(response)) {
          seatsFromApi = response; // Если API возвращает массив мест напрямую
        } else if (response.seats && Array.isArray(response.seats)) {
          seatsFromApi = response.seats; // Если есть поле seats
        } else if (response.seatList && Array.isArray(response.seatList)) {
          seatsFromApi = response.seatList; // Если есть поле seatList
        } else {
          console.warn('Неизвестный формат ответа от API:', response);
          // Генерируем базовые места по размерам зала
          this.generateBasicSeats();
          this.loadBookedSeats();
          return;
        }
        
        // Создаем UI места с базовой ценой из зала
        this.seats = seatsFromApi.map(seat => ({
          seat: {
            ...seat,
            basePrice: this.hall?.basePrice || 350,
            priceMultiplier: seat.priceMultiplier || this.getPriceMultiplierBySeatType(seat.seatType)
          },
          isSelected: false,
          isBooked: false
        }));
        
        // Загружаем забронированные места
        this.loadBookedSeats();
      },
      error: (error) => {
        console.warn('Не удалось загрузить схему мест, используем базовую', error);
        this.generateBasicSeats();
        this.loadBookedSeats();
      }
    });
  }

  loadBookedSeats(): void {
    if (!this.session || !this.session.bookingList) {
      this.isLoading = false;
      return;
    }
    
    // Собираем все ID забронированных мест из всех бронирований сеанса
    const bookedSeatIds = new Set<number>();
    
    this.session.bookingList.forEach((booking: any) => {
      if (booking.ticketList && Array.isArray(booking.ticketList)) {
        booking.ticketList.forEach((ticket: any) => {
          bookedSeatIds.add(ticket.seatId);
        });
      }
    });
    
    // Помечаем места как забронированные
    this.seats.forEach(seatUI => {
      seatUI.isBooked = bookedSeatIds.has(seatUI.seat.seatId);
    });
    
    this.isLoading = false;
  }

  private generateBasicSeats(): void {
    if (!this.hall) return;
    
    let seatId = 1;
    const seats: SeatUI[] = [];
    
    for (let row = 1; row <= this.hall.rowsCount; row++) {
      for (let seatNum = 1; seatNum <= this.hall.seatsPerRow; seatNum++) {
        const seatType = this.getSeatTypeForPosition(row, seatNum);
        const priceMultiplier = this.getPriceMultiplierBySeatType(seatType);
        
        seats.push({
          seat: {
            seatId: seatId++,
            rowNumber: row,
            seatNumber: seatNum,
            seatType: seatType,
            priceMultiplier: priceMultiplier,
            hallId: this.hall.hallId,
            hallName: this.hall.hallName,
            basePrice: this.hall.basePrice,
            status: 'AVAILABLE'
          },
          isSelected: false,
          isBooked: false
        });
      }
    }
    
    this.seats = seats;
  }

  private getSeatTypeForPosition(row: number, seatNum: number): string {
    // VIP места обычно в центральных рядах
    if ((row === 3 || row === 4) && (seatNum >= 4 && seatNum <= 8)) {
      return 'vip';
    }
    // Заблокированные места (например, технические)
    if (row === this.hall?.rowsCount && (seatNum === 1 || seatNum === this.hall?.seatsPerRow)) {
      return 'blocked';
    }
    return 'standard';
  }

  private getPriceMultiplierBySeatType(seatType: string): number {
    switch(seatType) {
      case 'vip': return 1.5;
      case 'blocked': return 0;
      default: return 1.0;
    }
  }

  private handleError(message: string, error: any): void {
    console.error(message, error);
    this.errorMessage = `${message}: ${error.message || 'Неизвестная ошибка'}`;
    this.isLoading = false;
  }

  getSeatsByRow(row: number): SeatUI[] {
    return this.seats.filter(seatUI => seatUI.seat.rowNumber === row);
  }

  getRows(): number[] {
    if (!this.hall) return [];
    const maxRow = Math.max(...this.seats.map(seat => seat.seat.rowNumber));
    return Array.from({length: maxRow}, (_, i) => i + 1);
  }

  selectSeat(seatUI: SeatUI): void {
    if (seatUI.isBooked || seatUI.seat.seatType === 'blocked') return;
    
    seatUI.isSelected = !seatUI.isSelected;
    
    if (seatUI.isSelected) {
      this.selectedSeats.push(seatUI.seat);
    } else {
      this.selectedSeats = this.selectedSeats.filter(s => s.seatId !== seatUI.seat.seatId);
    }
  }

  getTotalPrice(): number {
    return this.selectedSeats.reduce((total, seat) => {
      const basePrice = seat.basePrice || 350;
      const multiplier = seat.priceMultiplier || 1.0;
      return total + (basePrice * multiplier);
    }, 0);
  }

  getSelectedSeatsText(): string {
    if (this.selectedSeats.length === 0) return 'Места не выбраны';
    
    const seatsByRow = this.groupSeatsByRow();
    return Object.keys(seatsByRow)
      .map(row => `Ряд ${row}: ${seatsByRow[+row].join(', ')}`)
      .join('; ');
  }

  private groupSeatsByRow(): {[row: number]: number[]} {
    const groups: {[row: number]: number[]} = {};
    
    this.selectedSeats.forEach(seat => {
      if (!groups[seat.rowNumber]) {
        groups[seat.rowNumber] = [];
      }
      groups[seat.rowNumber].push(seat.seatNumber);
    });
    
    // Сортируем места в ряду
    Object.keys(groups).forEach(row => {
      groups[+row].sort((a, b) => a - b);
    });
    
    return groups;
  }

  bookSeats(): void {
    if (this.selectedSeats.length === 0) {
      alert('Выберите хотя бы одно место');
      return;
    }

    if (!this.authService.isAuthenticated()) {
      alert('Для бронирования необходимо войти в систему');
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      alert('Ошибка: пользователь не найден');
      return;
    }

    this.isBooking = true;

 
    const bookingData: BookingDto = {
      bookingId: 0, 
      userId: currentUser.userId,
      sessionId: this.sessionId,
      bookingTime: '2024-12-25T18:30:00',
      totalCost: 12,
      status: 'active'
    };

    console.log('Создаем бронирование:', bookingData);

    // 1. Создаем бронирование
    this.bookingService.createBooking(bookingData).subscribe({
      next: (createdBooking) => {
        console.log('Бронирование создано:', createdBooking);
        
        // 2. Создаем билеты для каждого выбранного места
        this.createTicketsForBooking(createdBooking.bookingId);
      },
      error: (error) => {
        console.error('Ошибка создания бронирования:', error);
        alert(`Ошибка бронирования: ${error.error?.message || error.message || 'Неизвестная ошибка'}`);
        this.isBooking = false;
      }
    });
  }

  createTicketsForBooking(bookingId: number): void {
    const ticketPromises = this.selectedSeats.map(seat => {
      const ticketData: TicketDto = {
        ticketId: 0, // Будет установлено на бэкенде
        creationDate: new Date().toISOString(), // Может быть установлено на бэкенде
        price: this.getSeatPrice(seat),
        ticketCode: this.generateTicketCode(),
        seatId: seat.seatId,
        bookingId: bookingId
      };

      console.log('Создаем билет:', ticketData);
      return this.ticketService.createTicket(ticketData).toPromise();
    });

    // Ждем создания всех билетов
    Promise.all(ticketPromises)
      .then((createdTickets) => {
        console.log('Все билеты созданы:', createdTickets);
        
        // После успешного создания всех билетов переходим на страницу подтверждения
        this.navigateToConfirmation(bookingId);
      })
      .catch((error) => {
        console.error('Ошибка создания билетов:', error);
        alert(`Ошибка создания билетов: ${error.error?.message || error.message || 'Неизвестная ошибка'}`);
        this.isBooking = false;
      });
  }

  private navigateToConfirmation(bookingId: number): void {
    // Загружаем полную информацию о бронировании
    this.bookingService.getBookingById(bookingId).subscribe({
      next: (booking) => {
        // Получаем билеты для этого бронирования
        this.ticketService.getTicketsByBooking(bookingId).subscribe({
          next: (tickets) => {
            this.router.navigate(['/booking', booking.bookingId, 'ticket'], {
              state: {
                booking: {
                  ...booking,
                  ticketList: tickets
                },
                selectedSeats: this.selectedSeats,
                session: this.session,
                movie: this.movie,
                hall: this.hall,
                totalPrice: this.getTotalPrice()
              }
            });
            this.isBooking = false;
          },
          error: (error) => {
            console.error('Ошибка загрузки билетов:', error);
            this.router.navigate(['/confirm'], {
              state: {
                booking: booking,
                selectedSeats: this.selectedSeats,
                session: this.session,
                movie: this.movie,
                hall: this.hall,
                totalPrice: this.getTotalPrice()
              }
            });
            this.isBooking = false;
          }
        });
      },
      error: (error) => {
        console.error('Ошибка загрузки бронирования:', error);
        // Переходим с базовой информацией
        this.router.navigate(['/confirm'], {
          state: {
            booking: {
              bookingId: bookingId,
              status: 'active',
              totalCost: this.getTotalPrice()
            },
            selectedSeats: this.selectedSeats,
            session: this.session,
            movie: this.movie,
            hall: this.hall,
            totalPrice: this.getTotalPrice()
          }
        });
        this.isBooking = false;
      }
    });
  }

  private generateTicketCode(): string {
    // Генерация уникального кода билета
    const timestamp = Date.now().toString(36);
    const random = Math.random().toString(36).substring(2, 8);
    return `TK-${timestamp}-${random}`.toUpperCase();
  }

  clearSelection(): void {
    this.seats.forEach(seatUI => {
      seatUI.isSelected = false;
    });
    this.selectedSeats = [];
  }

  goBack(): void {
    if (this.session?.filmId) {
      this.router.navigate(['/movie', this.session.filmId]);
    } else {
      this.router.navigate(['/']);
    }
  }

  getSeatPrice(seat: SeatDto): number {
    const basePrice = seat.basePrice || 350;
    const multiplier = seat.priceMultiplier || this.getPriceMultiplierBySeatType(seat.seatType);
    return Math.round(basePrice * multiplier);
  }

  getSeatStatus(seatUI: SeatUI): string {
    if (seatUI.isBooked) return 'taken';
    if (seatUI.seat.seatType === 'blocked') return 'blocked';
    if (seatUI.isSelected) return 'selected';
    if (seatUI.seat.seatType === 'vip') return 'vip';
    return 'available';
  }
}