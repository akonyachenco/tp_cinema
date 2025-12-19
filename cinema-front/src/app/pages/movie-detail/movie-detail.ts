import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MovieService } from '../../core/services/movie.service';
import { SessionService } from '../../core/services/session.service';
import { FilmDto, SessionDto } from '../../shared/models';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './movie-detail.html',
  styleUrls: ['./movie-detail.css']
})
export class MovieDetailComponent implements OnInit {
  movie: FilmDto | null = null;
  sessions: SessionDto[] = [];
  isLoading = true;
  activeDateFilter: 'today' | 'tomorrow' | 'week' = 'today';
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private movieService: MovieService,
    private sessionService: SessionService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const movieId = +params['id'];
      if (movieId) {
        this.loadMovie(movieId);
        this.loadSessions(movieId);
      }
    });
  }

  loadMovie(movieId: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.movieService.getMovieById(movieId).subscribe({
      next: (movie) => {
        this.movie = movie || null;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка загрузки фильма:', error);
        this.errorMessage = 'Ошибка загрузки информации о фильме';
        this.isLoading = false;
      }
    });
  }

  loadSessions(movieId: number): void {
    this.sessionService.getSessionsByMovie(movieId).subscribe({
      next: (sessions) => {
        this.sessions = sessions;
      },
      error: (error) => {
        console.error('Ошибка загрузки сеансов:', error);
        // Можно добавить уведомление пользователю
      }
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
    // В идеале нужно загружать информацию о зале через hallService
    // Здесь оставлю заглушку, которую можно заменить реальным вызовом
    const halls: { [key: number]: string } = {
      1: 'Большой зал (IMAX)',
      2: 'Малый зал',
      3: 'VIP зал',
      4: '4DX зал'
    };
    return halls[hallId] || `Зал ${hallId}`;
  }

  getHallType(hallId: number): string {
    // Здесь также можно добавить загрузку информации о зале
    const types: { [key: number]: string } = {
      1: 'IMAX • Dolby Atmos',
      2: '2D • Стандарт',
      3: '2D • Комфорт',
      4: '4DX • Эффекты'
    };
    return types[hallId] || '2D';
  }

  getTicketPrice(hallId: number): number {
    // Цены лучше получать с бэкенда или использовать информацию из сеанса/зала
    const prices: { [key: number]: number } = {
      1: 700, // IMAX
      2: 350, // Стандарт
      3: 500, // Комфорт
      4: 900  // 4DX
    };
    return prices[hallId] || 400;
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
}