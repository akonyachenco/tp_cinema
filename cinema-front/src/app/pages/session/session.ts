// pages/session/session.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { HallService } from '../../core/services/hall.service';
import { MovieService } from '../../core/services/movie.service';
import { BookingService } from '../../core/services/booking.service';
import { AuthService } from '../../core/services/auth.service';
import { 
  SessionDto, 
  HallDto, 
  FilmDto, 
  SeatDto, 
  BookSeatsRequest,
  BookingDto
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
  errorMessage = '';
  
  // Mock данные для демонстрации (удалите когда будет бэкенд)
  private mockHalls: HallDto[] = [
    {
      hallId: 1,
      status: 'active',
      basePrice: 350,
      rows_count: 8,
      seatsPerRow: 12,
      hallName: 'Большой зал (IMAX)',
      hallType: 'IMAX'
    },
    {
      hallId: 2,
      status: 'active',
      basePrice: 300,
      rows_count: 6,
      seatsPerRow: 10,
      hallName: 'Малый зал',
      hallType: '2D'
    },
    {
      hallId: 3,
      status: 'active',
      basePrice: 500,
      rows_count: 4,
      seatsPerRow: 8,
      hallName: 'VIP зал',
      hallType: 'VIP'
    }
  ];

  private mockSeats: SeatDto[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: SessionService,
    private hallService: HallService,
    private movieService: MovieService,
    private bookingService: BookingService,
    private authService: AuthService
  ) {
    this.generateMockSeats();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.sessionId = +params['id'];
      this.loadSessionData();
    });
  }

  loadSessionData(): void {
    this.isLoading = true;
    
    // Загружаем сеанс (мок данные)
    setTimeout(() => {
      this.session = {
        sessionId: this.sessionId,
        status: 'active',
        dateTime: new Date().toISOString(),
        filmId: 2,
        hallId: 2
      };
      
      // Загружаем зал
      this.hall = this.mockHalls.find(h => h.hallId === 1) || this.mockHalls[1];
      
      // Загружаем фильм
      this.movieService.getMovieById(1).subscribe(movie => {
        this.movie = movie || null;
        
        // Загружаем места
        this.seats = this.mockSeats.map(seat => ({
          seat: seat,
          isSelected: false,
          isBooked: this.isSeatBooked(seat.seatId)
        }));
        
        this.isLoading = false;
      });
    }, 500);
  }

  private generateMockSeats(): void {
    const hall = this.mockHalls[0];
    let seatId = 1;
    
    for (let row = 1; row <= hall.rows_count; row++) {
      for (let seatNum = 1; seatNum <= hall.seatsPerRow; seatNum++) {
        const isVip = (row === 3 || row === 4) && (seatNum >= 4 && seatNum <= 8);
        
        this.mockSeats.push({
          seatId: seatId++,
          rowNumber: row,
          seatNumber: seatNum,
          seatType: isVip ? 'vip' : 'standard',
          priceMultiplier: isVip ? 1.5 : 1.0,
          hallId: hall.hallId,
          hallName: hall.hallName,
          basePrice: hall.basePrice
        });
      }
    }
  }

  private isSeatBooked(seatId: number): boolean {
    // Mock: 30% мест заняты
    const bookedSeats = [3, 7, 10, 15, 22, 25, 30, 35, 40, 45, 50, 55, 60];
    return bookedSeats.includes(seatId);
  }

  getSeatsByRow(row: number): SeatUI[] {
    return this.seats.filter(seatUI => seatUI.seat.rowNumber === row);
  }

  getRows(): number[] {
    if (!this.hall) return [];
    return Array.from({length: this.hall.rows_count}, (_, i) => i + 1);
  }

  selectSeat(seatUI: SeatUI): void {
    if (seatUI.isBooked) return;
    
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
      return total + (basePrice * seat.priceMultiplier);
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

    const bookingRequest: BookSeatsRequest = {
      sessionId: this.sessionId,
      seatIds: this.selectedSeats.map(seat => seat.seatId)
    };

    // Mock запрос
    setTimeout(() => {
      const mockBooking: BookingDto = {
        bookingId: Math.floor(Math.random() * 1000) + 1,
        bookingTime: new Date().toISOString(),
        totalCost: this.getTotalPrice(),
        userId: 1,
        sessionId: this.sessionId,
        status: 'confirmed'
      };

      this.router.navigate(['/confirm'], {
        state: {
          booking: mockBooking,
          selectedSeats: this.selectedSeats,
          session: this.session,
          movie: this.movie,
          hall: this.hall,
          totalPrice: this.getTotalPrice()
        }
      });
    }, 1000);
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
    return basePrice * seat.priceMultiplier;
  }

  getSeatStatus(seatUI: SeatUI): string {
    if (seatUI.isBooked) return 'taken';
    if (seatUI.isSelected) return 'selected';
    if (seatUI.seat.seatType === 'vip') return 'vip';
    return 'available';
  }
}