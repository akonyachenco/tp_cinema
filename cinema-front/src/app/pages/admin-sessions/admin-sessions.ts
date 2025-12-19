// admin-sessions.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../core/services/session.service';
import { MovieService } from '../../core/services/movie.service';
import { HallService } from '../../core/services/hall.service';
import { SessionDto, FilmDto, HallDto } from '../../shared/models';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-sessions',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-sessions.html',
  styleUrls: ['./admin-sessions.css']
})
export class AdminSessions implements OnInit {
  sessions: SessionDto[] = [];
  filteredSessions: any[] = []; // Массив с доп. информацией
  filterMode: 'all' | 'today' | 'week' = 'all';
  filmId: number | null = null;
  isLoading = true;
  errorMessage = '';
  
  // Кэшированные данные
  filmsCache: Map<number, FilmDto> = new Map();
  hallsCache: Map<number, HallDto> = new Map();
  
  // Фильтры
  dateFilter: string = '';
  statusFilter: string = '';
  filmNameFilter: string = '';
  hallNameFilter: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: SessionService,
    private movieService: MovieService,
    private hallService: HallService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.filmId = params['filmId'] ? +params['filmId'] : null;
      this.loadSessions();
    });
  }

  loadSessions() {
    this.isLoading = true;
    this.errorMessage = '';
    
    const loadObservable = this.filmId
      ? this.sessionService.getSessionsByMovie(this.filmId)
      : this.sessionService.getAllSessions();
    
    loadObservable.subscribe({
      next: (sessions) => {
        this.sessions = sessions;
        this.loadAdditionalData();
      },
      error: (error) => {
        console.error('Ошибка загрузки сеансов:', error);
        this.errorMessage = 'Не удалось загрузить сеансы';
        this.isLoading = false;
      }
    });
  }

  loadAdditionalData() {
    // Собираем уникальные ID фильмов и залов
    const filmIds = [...new Set(this.sessions.map(s => s.filmId))];
    const hallIds = [...new Set(this.sessions.map(s => s.hallId))];
    
    let completedRequests = 0;
    const totalRequests = filmIds.length + hallIds.length;
    
    // Если нет данных для загрузки
    if (totalRequests === 0) {
      this.isLoading = false;
      this.applyFilters();
      return;
    }
    
    // Загружаем фильмы
    filmIds.forEach(filmId => {
      if (!this.filmsCache.has(filmId)) {
        this.movieService.getMovieById(filmId).subscribe({
          next: (film) => {
            if (film) {
              this.filmsCache.set(filmId, film);
            }
            completedRequests++;
            this.checkAllDataLoaded(completedRequests, totalRequests);
          },
          error: (error) => {
            console.error(`Ошибка загрузки фильма ${filmId}:`, error);
            completedRequests++;
            this.checkAllDataLoaded(completedRequests, totalRequests);
          }
        });
      } else {
        completedRequests++;
        this.checkAllDataLoaded(completedRequests, totalRequests);
      }
    });
    
    // Загружаем залы
    hallIds.forEach(hallId => {
      if (!this.hallsCache.has(hallId)) {
        this.hallService.getHallById(hallId).subscribe({
          next: (hall) => {
            this.hallsCache.set(hallId, hall);
            completedRequests++;
            this.checkAllDataLoaded(completedRequests, totalRequests);
          },
          error: (error) => {
            console.error(`Ошибка загрузки зала ${hallId}:`, error);
            completedRequests++;
            this.checkAllDataLoaded(completedRequests, totalRequests);
          }
        });
      } else {
        completedRequests++;
        this.checkAllDataLoaded(completedRequests, totalRequests);
      }
    });
  }

  checkAllDataLoaded(completed: number, total: number) {
    if (completed >= total) {
      this.isLoading = false;
      this.applyFilters();
    }
  }

  applyFilters() {
    let result = this.sessions.map(session => {
      const film = this.filmsCache.get(session.filmId);
      const hall = this.hallsCache.get(session.hallId);
      
      return {
        ...session,
        filmTitle: film?.title || `Фильм #${session.filmId}`,
        hallName: hall?.hallName || `Зал #${session.hallId}`,
        filmData: film,
        hallData: hall
      };
    });
    
    // Фильтр по дате (быстрые фильтры)
    if (this.filterMode === 'today') {
      const today = new Date().toISOString().split('T')[0];
      result = result.filter(s => s.dateTime.split('T')[0] === today);
    } else if (this.filterMode === 'week') {
      const now = new Date();
      const nextWeek = new Date();
      nextWeek.setDate(now.getDate() + 7);
      result = result.filter(s => {
        const d = new Date(s.dateTime);
        return d >= now && d <= nextWeek;
      });
    }
    
    // Фильтр по статусу
    if (this.statusFilter) {
      result = result.filter(s => s.status === this.statusFilter);
    }
    
    // Фильтр по дате (ручной)
    if (this.dateFilter) {
      result = result.filter(s => s.dateTime.split('T')[0] === this.dateFilter);
    }
    
    // Фильтр по названию фильма
    if (this.filmNameFilter) {
      const searchTerm = this.filmNameFilter.toLowerCase().trim();
      result = result.filter(s => {
        const filmTitle = s.filmTitle.toLowerCase();
        return filmTitle.includes(searchTerm);
      });
    }
    
    // Фильтр по названию зала
    if (this.hallNameFilter) {
      const searchTerm = this.hallNameFilter.toLowerCase().trim();
      result = result.filter(s => {
        const hallName = s.hallName.toLowerCase();
        return hallName.includes(searchTerm);
      });
    }
    
    this.filteredSessions = result;
  }

  setFilter(mode: 'all' | 'today' | 'week') {
    this.filterMode = mode;
    this.applyFilters();
  }

  onStatusFilterChange() {
    this.applyFilters();
  }

  onDateFilterChange() {
    this.applyFilters();
  }

  onFilmNameFilterChange() {
    this.applyFilters();
  }

  onHallNameFilterChange() {
    this.applyFilters();
  }

  editSession(id: number) {
    // Переход на страницу редактирования сеанса
    this.router.navigate(['/admin/sessions/add'], { 
      queryParams: { edit: true, id: id } 
    });
  }

  deleteSession(id: number) {
    if (!confirm('Вы уверены, что хотите удалить этот сеанс?')) return;
    
    this.sessionService.deleteSession(id).subscribe({
      next: () => {
        this.sessions = this.sessions.filter(s => s.sessionId !== id);
        this.applyFilters();
      },
      error: (error) => {
        console.error('Ошибка удаления сеанса:', error);
        alert('Не удалось удалить сеанс');
      }
    });
  }

  refreshSessions() {
    // Очищаем кэш при обновлении
    this.filmsCache.clear();
    this.hallsCache.clear();
    this.loadSessions();
  }

  clearFilters() {
    this.filterMode = 'all';
    this.statusFilter = '';
    this.dateFilter = '';
    this.filmNameFilter = '';
    this.hallNameFilter = '';
    this.applyFilters();
  }

  formatDateTime(dateTime: string): string {
    return new Date(dateTime).toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatusText(status: string): string {
    const statusMap: { [key: string]: string } = {
      'active': 'Активен',
      'cancelled': 'Отменен',
      'completed': 'Завершен'
    };
    return statusMap[status] || status;
  }
}