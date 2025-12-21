import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Subject, takeUntil, map, forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { BookingService } from '../../core/services/booking.service';
import { UserDto, BookingDto, SessionDto, FilmDto } from '../../shared/models';
import { SessionService } from '../../core/services/session.service';
import { MovieService } from '../../core/services/movie.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './account.html',
  styleUrls: ['./account.css']
})
export class AccountComponent implements OnInit, OnDestroy {
  user: UserDto | null = null;
  bookings: (BookingDto & {
    canCancel?: boolean;
    session?: SessionDto;
    sessionTime?: Date;
    filmTitle?: string;
    hallName?: string;
  })[] = [];

  filteredBookings: (BookingDto & {
    canCancel?: boolean;
    session?: SessionDto;
    sessionTime?: Date;
    filmTitle?: string;
    hallName?: string;
  })[] = [];

  activeStatusFilter: string = 'all';
  isLoading = true;
  activeTab: 'profile' | 'bookings' = 'bookings';

  private destroy$ = new Subject<void>();

  constructor(
    private auth: AuthService,
    private bookingService: BookingService,
    private sessionService: SessionService,
    private movieService: MovieService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get activeBookings() {
    return this.filteredBookings.filter(b =>
      this.isStatusActive(b.status)
    );
  }

  get completedBookings() {
    return this.filteredBookings.filter(b =>
      this.isStatusCompleted(b.status)
    );
  }

  get cancelledBookings() {
    return this.filteredBookings.filter(b =>
      this.isStatusCancelled(b.status)
    );
  }

  applyStatusFilter(status: string): void {
    this.activeStatusFilter = status;
    this.filterAndSortBookings();
  }

  filterAndSortBookings(): void {
    let filtered = this.bookings;

    if (this.activeStatusFilter !== 'all') {
      filtered = filtered.filter(booking => {
        switch (this.activeStatusFilter) {
          case 'active':
            return this.isStatusActive(booking.status);
          case 'cancelled':
            return this.isStatusCancelled(booking.status);
          case 'completed':
            return this.isStatusCompleted(booking.status);
          default:
            return true;
        }
      });
    }

    this.filteredBookings = this.sortBookings(filtered);
  }

  private sortBookings(bookings: any[]): any[] {
    return bookings.sort((a, b) => {
      const statusPriority = this.getStatusPriority(b.status) - this.getStatusPriority(a.status);

      if (statusPriority !== 0) {
        return statusPriority;
      }

      const timeA = a.sessionTime || new Date(a.bookingTime);
      const timeB = b.sessionTime || new Date(b.bookingTime);

      return timeA.getTime() - timeB.getTime();
    });
  }

  private getStatusPriority(status: string): number {
    if (this.isStatusActive(status)) {
      return 3; // Высший приоритет - активные
    } else if (this.isStatusCompleted(status)) {
      return 2; // Средний приоритет - завершённые
    } else if (this.isStatusCancelled(status)) {
      return 1; // Низший приоритет - отменённые
    }

    return 0;
  }

  private isStatusActive(status: string): boolean {
    const statusLower = status.toLowerCase();
    return statusLower === 'active' || statusLower === 'активно';
  }

  private isStatusCompleted(status: string): boolean {
    const statusLower = status.toLowerCase();
    return statusLower === 'completed' ||
      statusLower === 'завершено' ||
      statusLower === 'inactive' ||
      statusLower === 'неактивно' ||
      statusLower === 'expired' ||
      statusLower === 'просрочено';
  }

  private isStatusCancelled(status: string): boolean {
    const statusLower = status.toLowerCase();
    return statusLower === 'cancelled' ||
      statusLower === 'отменено' ||
      statusLower === 'отмена';
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

    if (!this.user) {
      this.isLoading = false;
      return;
    }

    this.bookingService.getUserBookings(this.user.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (bookings) => {
          this.loadSessionsAndFilmsForBookings(bookings);
        },
        error: (error) => {
          console.error('Ошибка загрузки бронирований:', error);
          this.isLoading = false;
          if (error.status === 401) {
            this.auth.logout();
            this.router.navigate(['/login']);
          }
        }
      });
  }

  loadSessionsAndFilmsForBookings(bookings: BookingDto[]): void {
    if (bookings.length === 0) {
      this.bookings = bookings;
      this.filteredBookings = bookings;
      this.isLoading = false;
      return;
    }

    const sessionRequests = bookings.map(booking => {
      return this.sessionService.getSessionById(booking.sessionId)
        .pipe(
          map(session => ({
            bookingId: booking.bookingId,
            session: session
          }))
        );
    });

    forkJoin(sessionRequests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (results) => {
          const filmIds = [...new Set(results
            .filter(r => r.session)
            .map(r => r.session.filmId))];

          const filmRequests = filmIds.map(filmId =>
            this.movieService.getMovieById(filmId)
          );

          forkJoin(filmRequests).subscribe({
            next: (films) => {
              const filmsMap = new Map<number, FilmDto>();
              films.forEach(film => {
                if (film) {
                  filmsMap.set(film.filmId, film);
                }
              });

              const enrichedBookings = bookings.map(booking => {
                const result = results.find(r => r.bookingId === booking.bookingId);
                const session = result?.session;
                const sessionTime = session ? new Date(session.dateTime) : undefined;
                const film = session ? filmsMap.get(session.filmId) : undefined;
                const filmTitle = film?.title || `Фильм #${session?.filmId}`;
                const canCancel = this.canCancelBooking(booking, sessionTime);

                // Приводим все статусы к трём основным
                const normalizedStatus = this.normalizeBookingStatus(booking.status);

                return {
                  ...booking,
                  status: normalizedStatus, // Заменяем статус на нормализованный
                  session: session,
                  sessionTime: sessionTime,
                  filmTitle: filmTitle,
                  canCancel: canCancel
                };
              });

              this.bookings = enrichedBookings;
              this.filterAndSortBookings();
              this.isLoading = false;
            },
            error: (filmError) => {
              console.error('Ошибка загрузки фильмов:', filmError);
              this.processBookingsWithoutFilms(bookings, results);
            }
          });
        },
        error: (error) => {
          console.error('Ошибка загрузки информации о сеансах:', error);
          this.bookings = bookings;
          this.isLoading = false;
        }
      });
  }

  private normalizeBookingStatus(status: string): string {
    if (this.isStatusActive(status)) {
      return 'active';
    } else if (this.isStatusCompleted(status)) {
      return 'completed';
    } else if (this.isStatusCancelled(status)) {
      return 'cancelled';
    } else {
      // По умолчанию считаем завершённым
      return 'completed';
    }
  }

  processBookingsWithoutFilms(bookings: BookingDto[], results: any[]): void {
    const enrichedBookings = bookings.map(booking => {
      const result = results.find(r => r.bookingId === booking.bookingId);
      const session = result?.session;
      const sessionTime = session ? new Date(session.dateTime) : undefined;
      const filmTitle = session ? `Фильм #${session.filmId}` : 'Фильм';
      const canCancel = this.canCancelBooking(booking, sessionTime);

      // Нормализуем статус
      const normalizedStatus = this.normalizeBookingStatus(booking.status);

      return {
        ...booking,
        status: normalizedStatus,
        session: session,
        sessionTime: sessionTime,
        filmTitle: filmTitle,
        canCancel: canCancel
      };
    });

    this.bookings = enrichedBookings;
    this.filterAndSortBookings();
    this.isLoading = false;
  }

  canCancelBooking(booking: BookingDto, sessionTime?: Date): boolean {
    // Можно отменять только активные бронирования
    if (!this.isStatusActive(booking.status)) return false;

    const targetTime = sessionTime || new Date(booking.bookingTime);
    const now = new Date();
    const hoursDiff = (targetTime.getTime() - now.getTime()) / (1000 * 60 * 60);
    return hoursDiff > 1;
  }

  getBookingStatusText(status: string): string {
    if (this.isStatusActive(status)) {
      return 'Активно';
    } else if (this.isStatusCompleted(status)) {
      return 'Завершено';
    } else if (this.isStatusCancelled(status)) {
      return 'Отменено';
    }

    // По умолчанию считаем завершённым
    return 'Завершено';
  }

  getBookingStatusClass(status: string): string {
    if (this.isStatusActive(status)) {
      return 'status-active';
    } else if (this.isStatusCompleted(status)) {
      return 'status-completed';
    } else if (this.isStatusCancelled(status)) {
      return 'status-cancelled';
    }

    // По умолчанию считаем завершённым
    return 'status-completed';
  }

  setActiveTab(tab: 'profile' | 'bookings'): void {
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

  cancelBooking(bookingId: number): void {
    if (confirm('Вы уверены, что хотите отменить бронирование?')) {
      this.bookingService.cancelBooking(bookingId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            const booking = this.bookings.find(b => b.bookingId === bookingId);
            if (booking) {
              booking.status = 'cancelled';
              booking.canCancel = false;
            }
            this.filterAndSortBookings();
            alert('Бронирование успешно отменено!');
          },
          error: (error) => {
            console.error('Ошибка отмены бронирования:', error);

            if (error.status === 400) {
              alert('Нельзя отменить это бронирование. Возможно, прошло слишком много времени.');
            } else if (error.status === 404) {
              alert('Бронирование не найдено.');
            } else if (error.status === 401) {
              this.auth.logout();
              this.router.navigate(['/login']);
            } else {
              alert('Не удалось отменить бронирование. Попробуйте позже.');
            }
          }
        });
    }
  }

  formatSessionTime(sessionTime?: Date): string {
    if (!sessionTime) return 'Время не указано';
    const sessionDate = new Date(sessionTime);

    return sessionDate.toLocaleString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year:  'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isFilterActive(filter: string): boolean {
    return this.activeStatusFilter === filter;
  }
}
