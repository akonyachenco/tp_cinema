// src/app/pages/movie-detail/movie-detail.ts
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
    
    // Временные мок-данные (замените на реальный запрос)
    const mockMovies: FilmDto[] = [
      {
        filmId: 1,
        title: 'Дюна: Часть вторая',
        description: 'Продолжение эпической саги о пустынной планете Арракис. Пол Атрейдес объединяется с Фрименами, чтобы отомстить за свою семью и предотвратить ужасное будущее, которое он предвидел. На фоне эпических битв и политических интриг он должен сделать выбор между любовью и судьбой человечества.',
        duration: 166,
        ageRating: '16+',
        genres: ['Фантастика', 'Драма', 'Приключения', 'Экшен'],
        directorId: 1,
        countryId: 1,
        releaseDate: '2024-03-01',
        posterUrl: 'https://m.media-amazon.com/images/M/MV5BODI0YjNhNjUtYzE2MC00ZDI1LWE5OTgtODVmNDg1N2FiMTIxXkEyXkFqcGdeQXVyMTUzMTg2ODkz._V1_FMjpg_UX1000_.jpg',
        trailerUrl: 'https://www.youtube.com/watch?v=Way9Dexny3w',
        sessionList: []
      },
      {
        filmId: 2,
        title: 'Оппенгеймер',
        description: 'История американского учёного Джулиуса Роберта Оппенгеймера и его роли в разработке атомной бомбы. Фильм охватывает его студенческие годы, работу над Манхэттенским проектом и последующие слушания по допуску к секретной информации.',
        duration: 180,
        ageRating: '18+',
        genres: ['Биография', 'Драма', 'Исторический'],
        directorId: 2,
        countryId: 1,
        releaseDate: '2023-07-21',
        posterUrl: 'https://m.media-amazon.com/images/M/MV5BMDBmYTZjNjUtN2M1MS00MTQ2LTk2ODgtNzc2M2QyZGE5NTVjXkEyXkFqcGdeQXVyNzAwMjU2MTY@._V1_.jpg',
        trailerUrl: 'https://www.youtube.com/watch?v=bK6ldnjE3Y0',
        sessionList: []
      }
    ];

    const movie = mockMovies.find(m => m.filmId === movieId);
    
    setTimeout(() => {
      if (movie) {
        this.movie = movie;
      } else {
        this.errorMessage = 'Фильм не найден';
      }
      this.isLoading = false;
    }, 300);

    // Позже замените на:
    // this.movieService.getMovieById(movieId).subscribe({
    //   next: (movie) => {
    //     this.movie = movie;
    //     this.isLoading = false;
    //   },
    //   error: (error) => {
    //     this.errorMessage = 'Ошибка загрузки фильма';
    //     this.isLoading = false;
    //   }
    // });
  }

  loadSessions(movieId: number): void {
    // Временные мок-сеансы
    const mockSessions: SessionDto[] = [
      {
        sessionId: 101,
        status: 'active',
        dateTime: this.getTodayDate('19:00'),
        filmId: movieId,
        hallId: 1,
        bookingList: []
      },
      {
        sessionId: 102,
        status: 'active',
        dateTime: this.getTodayDate('21:30'),
        filmId: movieId,
        hallId: 2,
        bookingList: []
      },
      {
        sessionId: 103,
        status: 'active',
        dateTime: this.getTomorrowDate('15:00'),
        filmId: movieId,
        hallId: 1,
        bookingList: []
      },
      {
        sessionId: 104,
        status: 'active',
        dateTime: this.getTomorrowDate('18:00'),
        filmId: movieId,
        hallId: 3,
        bookingList: []
      },
      {
        sessionId: 105,
        status: 'active',
        dateTime: this.getDateInDays(2, '20:00'),
        filmId: movieId,
        hallId: 1,
        bookingList: []
      }
    ];

    setTimeout(() => {
      this.sessions = mockSessions;
    }, 400);
  }

  // Вспомогательные методы для дат
  private getTodayDate(time: string): string {
    const today = new Date();
    const [hours, minutes] = time.split(':');
    today.setHours(parseInt(hours), parseInt(minutes), 0, 0);
    return today.toISOString();
  }

  private getTomorrowDate(time: string): string {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const [hours, minutes] = time.split(':');
    tomorrow.setHours(parseInt(hours), parseInt(minutes), 0, 0);
    return tomorrow.toISOString();
  }

  private getDateInDays(days: number, time: string): string {
    const date = new Date();
    date.setDate(date.getDate() + days);
    const [hours, minutes] = time.split(':');
    date.setHours(parseInt(hours), parseInt(minutes), 0, 0);
    return date.toISOString();
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
    const date = new Date(dateTime);
    return date.toLocaleTimeString('ru-RU', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }

  formatSessionDate(dateTime: string): string {
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
  }

  getHallName(hallId: number): string {
    const halls: { [key: number]: string } = {
      1: 'Большой зал (IMAX)',
      2: 'Малый зал',
      3: 'VIP зал',
      4: '4DX зал'
    };
    return halls[hallId] || `Зал ${hallId}`;
  }

  getHallType(hallId: number): string {
    const types: { [key: number]: string } = {
      1: 'IMAX • Dolby Atmos',
      2: '2D • Стандарт',
      3: '2D • Комфорт',
      4: '4DX • Эффекты'
    };
    return types[hallId] || '2D';
  }

  getTicketPrice(hallId: number): number {
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
    const sessionTime = new Date(session.dateTime);
    const now = new Date();
    const hoursDiff = (sessionTime.getTime() - now.getTime()) / (1000 * 60 * 60);
    return hoursDiff < 1; // Меньше часа до сеанса
  }
}