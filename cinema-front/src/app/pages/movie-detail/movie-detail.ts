import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import { MovieService } from '../../core/services/movie.service';
import { SessionService } from '../../core/services/session.service';
import { HallService } from '../../core/services/hall.service';
import { FilmDto, SessionDto, HallDto, FilmInfoListDto, DirectorDto, CountryDto } from '../../shared/models';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './movie-detail.html',
  styleUrls: ['./movie-detail.css']
})
export class MovieDetailComponent implements OnInit, OnDestroy {
  movie: FilmDto | null = null;
  sessions: SessionDto[] = [];
  halls: Map<number, HallDto> = new Map();
  directors: DirectorDto[] = [];
  countries: CountryDto[] = [];
  isLoading = true;
  activeDateFilter: 'today' | 'tomorrow' | 'week' = 'today';
  errorMessage = '';
  
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private movieService: MovieService,
    private sessionService: SessionService,
    private hallService: HallService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const movieId = +params['id'];
      if (movieId) {
        this.loadMovieData(movieId);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadMovieData(movieId: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    // Загружаем фильм, сеансы и информацию о режиссерах/странах параллельно
    forkJoin({
      movie: this.movieService.getMovieById(movieId),
      sessions: this.sessionService.getSessionsByMovie(movieId),
      info: this.movieService.getCountriesAndDirectors()
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: ({ movie, sessions, info }) => {
        if (!movie) {
          this.errorMessage = 'Фильм не найден';
          this.isLoading = false;
          return;
        }

        this.movie = movie;
        this.sessions = sessions;
        this.directors = info.directors || [];
        this.countries = info.countries || [];
        
        // Загружаем информацию о залах для сеансов
        this.loadHallsForSessions(sessions);
        
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка загрузки данных фильма:', error);
        this.errorMessage = 'Ошибка загрузки информации о фильме';
        this.isLoading = false;
      }
    });
  }

  loadHallsForSessions(sessions: SessionDto[]): void {
    // Собираем уникальные hallId из сеансов
    const uniqueHallIds = [...new Set(sessions.map(s => s.hallId))];
    
    // Загружаем информацию о каждом зале
    uniqueHallIds.forEach(hallId => {
      this.hallService.getHallById(hallId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (hall) => {
            this.halls.set(hallId, hall);
          },
          error: (error) => {
            console.error(`Ошибка загрузки зала ${hallId}:`, error);
          }
        });
    });
  }

  // Фильтрация сеансов по дате
  get filteredSessions(): SessionDto[] {
    if (!this.sessions.length) return [];
    
    const now = new Date();
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const weekLater = new Date();
    weekLater.setDate(weekLater.getDate() + 7);
    
    return this.sessions.filter(session => {
      const sessionDate = new Date(session.dateTime);
      
      switch(this.activeDateFilter) {
        case 'today':
          return sessionDate.toDateString() === now.toDateString();
        case 'tomorrow':
          return sessionDate.toDateString() === tomorrow.toDateString();
        case 'week':
          return sessionDate >= now && sessionDate <= weekLater;
        default:
          return true;
      }
    }).sort((a, b) => {
      // Сортируем по времени сеанса
      return new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime();
    });
  }

  setDateFilter(filter: 'today' | 'tomorrow' | 'week'): void {
    this.activeDateFilter = filter;
  }

  formatSessionTime(dateTime: string): string {
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

  formatSessionDate(dateTime: string): string {
    try {
      const date = new Date(dateTime);
      const today = new Date();
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      
      if (date.toDateString() === today.toDateString()) {
        return 'Сегодня';
      } else if (date.toDateString() === tomorrow.toDateString()) {
        return 'Завтра';
      } else {
        return date.toLocaleDateString('ru-RU', { 
          weekday: 'long',
          day: 'numeric',
          month: 'long'
        });
      }
    } catch (error) {
      return 'Дата не определена';
    }
  }

  getHallName(hallId: number): string {
    const hall = this.halls.get(hallId);
    return hall?.hallName || `Зал ${hallId}`;
  }

  getHallType(hallId: number): string {
    const hall = this.halls.get(hallId);
    return hall?.hallType || '2D';
  }

  getTicketPrice(hallId: number): number {
    const hall = this.halls.get(hallId);
    return hall?.basePrice || 400;
  }

  getDirectorName(directorId: number): string {
    const director = this.directors.find(d => d.directorId === directorId);
    return director?.directorNameAndSurname || 'Неизвестный режиссер';
  }

  getCountryName(countryId: number): string {
    const country = this.countries.find(c => c.countryId === countryId);
    return country?.countryName || 'Неизвестная страна';
  }

  navigateToBooking(sessionId: number): void {
    this.router.navigate(['/session', sessionId]);
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  isSessionSoon(session: SessionDto): boolean {
    try {
      const sessionTime = new Date(session.dateTime);
      const now = new Date();
      const hoursDiff = (sessionTime.getTime() - now.getTime()) / (1000 * 60 * 60);
      return hoursDiff < 1; // Меньше часа до сеанса
    } catch (error) {
      return false;
    }
  }

  // Проверка, есть ли сеансы в ближайшее время
  get hasUpcomingSessions(): boolean {
    return this.filteredSessions.length > 0;
  }

  // Получение ближайшего сеанса
  get nearestSession(): SessionDto | null {
    const upcoming = this.sessions
      .filter(s => new Date(s.dateTime) > new Date())
      .sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime());
    
    return upcoming.length > 0 ? upcoming[0] : null;
  }
}