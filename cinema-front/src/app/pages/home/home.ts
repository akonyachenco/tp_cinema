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
  standalone: true,
  imports: [
    CommonModule,
    MovieCardComponent,
    DatePipe
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

        // Отфильтруем только фильмы с будущими сеансами
        const moviesWithFutureSessions = this.filterMoviesWithFutureSessions(movies);

        console.log('🎯 Фильмы с будущими сеансами:', moviesWithFutureSessions.length);
        this.movies = moviesWithFutureSessions;
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

    const now = new Date();
    const todayStr = this.formatDate(now);

    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = this.formatDate(tomorrow);

    const dayAfterTomorrow = new Date(now);
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
          this.hasSessionOnDate(movie, todayStr) &&
          this.hasFutureSessionsOnDate(movie, todayStr)
        );
        break;

      case 'tomorrow':
        this.filteredMovies = this.movies.filter(movie =>
          this.hasSessionOnDate(movie, tomorrowStr) &&
          this.hasFutureSessionsOnDate(movie, tomorrowStr)
        );
        break;

      case 'upcoming':
        this.filteredMovies = this.movies.filter(movie =>
          this.hasFutureSession(movie, tomorrowStr)
        );
        break;

      case 'all':
        this.filteredMovies = this.movies.filter(movie =>
          this.hasAnyFutureSession(movie)
        );
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
          const sessionDate = new Date(session.dateTime);
          const sessionDateStr = this.formatDate(sessionDate);
          return sessionDateStr === todayStr && sessionDate >= new Date();
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
          const sessionDate = new Date(session.dateTime);
          const sessionDateStr = this.formatDate(sessionDate);
          return sessionDateStr === tomorrowStr;
        }).length;
      }
    });
    return count;
  }

  getUpcomingSessionCount(): number {
    const dayAfterTomorrow = new Date();
    dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);
    const dayAfterTomorrowStr = this.formatDate(dayAfterTomorrow);
    let count = 0;
    this.movies.forEach(movie => {
      if (movie.sessionList) {
        count += movie.sessionList.filter(session => {
          if (!session.dateTime) return false;
          const sessionDate = new Date(session.dateTime);
          const sessionDateStr = this.formatDate(sessionDate);
          return sessionDateStr >= dayAfterTomorrowStr;
        }).length;
      }
    });
    return count;
  }

  getSessionsForActiveFilter(movie: FilmDto): SessionDto[] {
    if (!movie.sessionList) return [];

    const now = new Date();
    const todayStr = this.formatDate(now);

    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = this.formatDate(tomorrow);

    const dayAfterTomorrow = new Date(now);
    dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);
    const dayAfterTomorrowStr = this.formatDate(dayAfterTomorrow);

    switch(this.activeFilter) {
      case 'today':
        return movie.sessionList.filter(session =>
          session.dateTime &&
          this.formatDate(new Date(session.dateTime)) === todayStr &&
          new Date(session.dateTime) >= now // Только будущие сеансы
        );

      case 'tomorrow':
        return movie.sessionList.filter(session =>
          session.dateTime &&
          this.formatDate(new Date(session.dateTime)) === tomorrowStr
        );

      case 'upcoming':
        return movie.sessionList.filter(session => {
          if (!session.dateTime) return false;
          const sessionDate = new Date(session.dateTime);
          const sessionDateStr = this.formatDate(sessionDate);
          return sessionDateStr >= dayAfterTomorrowStr;
        });

      default: // 'all'
        return movie.sessionList.filter(session =>
          session.dateTime && new Date(session.dateTime) >= now
        );
    }
  }

  getMovieSessionDates(movie: FilmDto): string[] {
    if (!movie.sessionList) return [];

    const now = new Date();
    const dates = movie.sessionList
      .filter(session => {
        if (!session.dateTime) return false;
        return new Date(session.dateTime) >= now;
      })
      .map(session => {
        return this.formatDate(new Date(session.dateTime!));
      })
      .filter(date => date !== '');

    // Уникальные даты, отсортированные
    return [...new Set(dates)].sort();
  }

  getMovieSessionsForDate(movie: FilmDto, dateStr: string): SessionDto[] {
    if (!movie.sessionList) return [];

    const now = new Date();
    return movie.sessionList.filter(session => {
      if (!session.dateTime) return false;
      const sessionDate = new Date(session.dateTime);
      return this.formatDate(sessionDate) === dateStr && sessionDate >= now;
    });
  }

  getMovieSessionDatesForCard(movie: FilmDto): {date: string, sessions: SessionDto[]}[] {
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

    // Преобразуем в массив и сортируем по дате
    return Object.keys(groupedSessions)
      .sort()
      .map(date => ({
        date: date,
        sessions: groupedSessions[date].sort((a, b) =>
          new Date(a.dateTime!).getTime() - new Date(b.dateTime!).getTime()
        )
      }));
  }

  selectSession(session: SessionDto): void {
    console.log('Выбран сеанс:', {
      id: session.sessionId,
      dateTime: new Date(session.dateTime).toLocaleString('ru-RU'),
      время: new Date(session.dateTime).toLocaleTimeString('ru-RU')
    });
  }

  // ================= PRIVATE METHODS =================

  private filterMoviesWithFutureSessions(movies: FilmDto[]): FilmDto[] {
    const now = new Date();
    return movies.filter(movie => {
      if (!movie.sessionList || movie.sessionList.length === 0) {
        return false;
      }

      // Проверяем, есть ли хотя бы один будущий сеанс
      return movie.sessionList.some(session => {
        if (!session.dateTime) return false;
        return new Date(session.dateTime) >= now;
      });
    });
  }

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

  private hasFutureSessionsOnDate(movie: FilmDto, targetDateStr: string): boolean {
    if (!movie.sessionList || movie.sessionList.length === 0) {
      return false;
    }

    const now = new Date();
    return movie.sessionList.some(session => {
      if (!session.dateTime) return false;
      const sessionDate = new Date(session.dateTime);
      const sessionDateStr = this.formatDate(sessionDate);
      return sessionDateStr === targetDateStr && sessionDate >= now;
    });
  }

  private hasFutureSession(movie: FilmDto, tomorrowStr: string): boolean {
    if (!movie.sessionList || movie.sessionList.length === 0) {
      return false;
    }

    const dayAfterTomorrow = new Date();
    dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);
    const dayAfterTomorrowStr = this.formatDate(dayAfterTomorrow);

    return movie.sessionList.some(session => {
      if (!session.dateTime) return false;
      const sessionDateStr = this.formatDate(new Date(session.dateTime));
      return sessionDateStr >= dayAfterTomorrowStr;
    });
  }

  private hasAnyFutureSession(movie: FilmDto): boolean {
    if (!movie.sessionList || movie.sessionList.length === 0) {
      return false;
    }

    const now = new Date();
    return movie.sessionList.some(session => {
      if (!session.dateTime) return false;
      return new Date(session.dateTime) >= now;
    });
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
