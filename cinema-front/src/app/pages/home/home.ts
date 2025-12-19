// pages/home/home.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MovieCardComponent } from '../../shared/components/movie-card/movie-card';
import { MovieService } from '../../core/services/movie.service';
import { FilmDto, SessionDto } from '../../shared/models';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
  standalone: true, // Добавляем standalone
  imports: [
    CommonModule,
    MovieCardComponent,
    DatePipe // Для использования | date в шаблоне
  ]
})
export class HomeComponent implements OnInit {
  movies: FilmDto[] = [];
  filteredMovies: FilmDto[] = [];
  isLoading = true;
  activeFilter: 'today' | 'tomorrow' | 'upcoming' | 'all' = 'today';

  constructor(private movieService: MovieService) {}

  ngOnInit(): void {
    this.loadMovies();
  }

  loadMovies(): void {
    this.isLoading = true;
    this.movieService.getAllMovies().subscribe({
      next: (movies) => {
        console.log('📦 Получены фильмы:', movies);

        // Отфильтруем только фильмы с сеансами
        const moviesWithSessions = movies.filter(movie =>
          movie.sessionList && movie.sessionList.length > 0
        );

        console.log('🎯 Фильмы с сеансами:', moviesWithSessions.length);
        this.movies = moviesWithSessions;
        this.setFilter('today');
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ Ошибка загрузки фильмов:', error);
        this.isLoading = false;
      }
    });
  }

  setFilter(filter: 'today' | 'tomorrow' | 'upcoming' | 'all'): void {
    this.activeFilter = filter;

    const today = new Date();
    const todayStr = this.formatDate(today);

    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = this.formatDate(tomorrow);

    const dayAfterTomorrow = new Date(today);
    dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);
    const dayAfterTomorrowStr = this.formatDate(dayAfterTomorrow);

    console.log('🗓️ Даты для фильтрации:', {
      сегодня: todayStr,
      завтра: tomorrowStr,
      послезавтра: dayAfterTomorrowStr
    });

    switch(filter) {
      case 'today':
        this.filteredMovies = this.movies.filter(movie =>
          this.hasSessionOnDate(movie, todayStr)
        );
        break;

      case 'tomorrow':
        this.filteredMovies = this.movies.filter(movie =>
          this.hasSessionOnDate(movie, tomorrowStr)
        );
        break;

      case 'upcoming':
        this.filteredMovies = this.movies.filter(movie =>
          this.hasFutureSession(movie, tomorrowStr)
        );
        break;

      case 'all':
        this.filteredMovies = [...this.movies];
        break;
    }

    console.log(`✅ Фильтр "${filter}": показано ${this.filteredMovies.length} из ${this.movies.length}`);
  }

  // ================= PUBLIC METHODS =================

  getCurrentDate(): Date {
    return new Date();
  }

  getTodaysSessionCount(): number {
    const todayStr = this.formatDate(new Date());
    let count = 0;
    this.movies.forEach(movie => {
      if (movie.sessionList) {
        count += movie.sessionList.filter(session => {
          if (!session.dateTime) return false;
          return this.formatDate(new Date(session.dateTime)) === todayStr;
        }).length;
      }
    });
    return count;
  }

  getTomorrowsSessionCount(): number {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = this.formatDate(tomorrow);
    let count = 0;
    this.movies.forEach(movie => {
      if (movie.sessionList) {
        count += movie.sessionList.filter(session => {
          if (!session.dateTime) return false;
          return this.formatDate(new Date(session.dateTime)) === tomorrowStr;
        }).length;
      }
    });
    return count;
  }

  getUpcomingSessionCount(): number {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = this.formatDate(tomorrow);
    let count = 0;
    this.movies.forEach(movie => {
      if (movie.sessionList) {
        count += movie.sessionList.filter(session => {
          if (!session.dateTime) return false;
          const sessionDateStr = this.formatDate(new Date(session.dateTime));
          return sessionDateStr > tomorrowStr;
        }).length;
      }
    });
    return count;
  }

  getSessionsForActiveFilter(movie: FilmDto): SessionDto[] {
    if (!movie.sessionList) return [];

    const todayStr = this.formatDate(new Date());
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = this.formatDate(tomorrow);
    const dayAfterTomorrow = new Date();
    dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);
    const dayAfterTomorrowStr = this.formatDate(dayAfterTomorrow);

    switch(this.activeFilter) {
      case 'today':
        return movie.sessionList.filter(session =>
          session.dateTime && this.formatDate(new Date(session.dateTime)) === todayStr
        );

      case 'tomorrow':
        return movie.sessionList.filter(session =>
          session.dateTime && this.formatDate(new Date(session.dateTime)) === tomorrowStr
        );

      case 'upcoming':
        return movie.sessionList.filter(session => {
          if (!session.dateTime) return false;
          const sessionDateStr = this.formatDate(new Date(session.dateTime));
          return sessionDateStr >= dayAfterTomorrowStr;
        });

      default:
        return movie.sessionList;
    }
  }

  getMovieSessionDates(movie: FilmDto): string[] {
    if (!movie.sessionList) return [];

    const dates = movie.sessionList
      .map(session => {
        if (!session.dateTime) return '';
        return this.formatDate(new Date(session.dateTime));
      })
      .filter(date => date !== '');

    // Уникальные даты, отсортированные
    return [...new Set(dates)].sort();
  }

  getMovieSessionsForDate(movie: FilmDto, dateStr: string): SessionDto[] {
    if (!movie.sessionList) return [];

    return movie.sessionList.filter(session => {
      if (!session.dateTime) return false;
      return this.formatDate(new Date(session.dateTime)) === dateStr;
    });
  }

  // Метод для получения даты сеансов для карточки фильма
  getMovieSessionDatesForCard(movie: FilmDto): {date: string, sessions: SessionDto[]}[] {
    if (this.activeFilter === 'all') {
      // Для фильтра "Все" показываем все даты с сеансами
      const dates = this.getMovieSessionDates(movie);
      if (dates.length === 0) return [];

      return dates.map(date => ({
        date: date,
        sessions: this.getMovieSessionsForDate(movie, date)
      }));
    } else {
      // Для конкретных фильтров показываем сеансы только для этого фильтра
      const sessions = this.getSessionsForActiveFilter(movie);
      if (sessions.length === 0) {
        return [];
      }

      // Группируем сеансы по датам
      const groupedSessions: {[key: string]: SessionDto[]} = {};
      sessions.forEach(session => {
        if (session.dateTime) {
          const dateStr = this.formatDate(new Date(session.dateTime));
          if (!groupedSessions[dateStr]) {
            groupedSessions[dateStr] = [];
          }
          groupedSessions[dateStr].push(session);
        }
      });

      // Преобразуем в массив
      return Object.keys(groupedSessions).map(date => ({
        date: date,
        sessions: groupedSessions[date]
      }));
    }
  }

  selectSession(session: SessionDto): void {
    console.log('Выбран сеанс:', {
      id: session.sessionId,
      dateTime: new Date(session.dateTime).toLocaleString('ru-RU'),
      время: new Date(session.dateTime).toLocaleTimeString('ru-RU')
    });
  }

  // ================= PRIVATE METHODS =================

  private hasSessionOnDate(movie: FilmDto, targetDateStr: string): boolean {
    if (!movie.sessionList || movie.sessionList.length === 0) {
      return false;
    }

    return movie.sessionList.some(session => {
      if (!session.dateTime) return false;
      const sessionDateStr = this.formatDate(new Date(session.dateTime));
      return sessionDateStr === targetDateStr;
    });
  }

  private hasFutureSession(movie: FilmDto, tomorrowStr: string): boolean {
    if (!movie.sessionList || movie.sessionList.length === 0) {
      return false;
    }

    return movie.sessionList.some(session => {
      if (!session.dateTime) return false;
      const sessionDateStr = this.formatDate(new Date(session.dateTime));
      return sessionDateStr > tomorrowStr;
    });
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
