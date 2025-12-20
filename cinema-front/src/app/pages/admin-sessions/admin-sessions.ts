// admin-sessions.ts
import { Component, OnInit, signal } from '@angular/core';
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
  sessions = signal<SessionDto[]>([]);
  filteredSessions = signal<any[]>([]);

  filterMode = signal<'all' | 'today' | 'week'>('all');
  filmId = signal<number | null>(null);
  isLoading = signal(true);
  errorMessage = signal('');

  dateFilter = signal('');
  statusFilter = signal('');
  filmNameFilter = signal('');
  hallNameFilter = signal('');

  filmsCache = new Map<number, FilmDto>();
  hallsCache = new Map<number, HallDto>();

  // Статусы, которые нельзя редактировать
  private readonly NON_EDITABLE_STATUSES = ['Активен', 'Завершен'];
  // Статусы, которые нельзя удалять
  private readonly NON_DELETABLE_STATUSES = ['Активен'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sessionService: SessionService,
    private movieService: MovieService,
    private hallService: HallService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      const filmId = params['filmId'] ? +params['filmId'] : null;
      this.filmId.set(filmId);
      this.loadSessions();
    });
  }

  loadSessions() {
    this.isLoading.set(true);
    this.errorMessage.set('');

    const loadObservable = this.filmId()
      ? this.sessionService.getSessionsByMovie(this.filmId()!)
      : this.sessionService.getAllSessions();

    loadObservable.subscribe({
      next: (sessions) => {
        this.sessions.set(sessions);
        this.loadAdditionalData();
      },
      error: (error) => {
        console.error('Ошибка загрузки сеансов:', error);
        this.errorMessage.set('Не удалось загрузить сеансы');
        this.isLoading.set(false);
      }
    });
  }

  loadAdditionalData() {
    const currentSessions = this.sessions();
    if (currentSessions.length === 0) {
      this.isLoading.set(false);
      this.applyFilters();
      return;
    }

    const filmIds = [...new Set(currentSessions.map(s => s.filmId))];
    const hallIds = [...new Set(currentSessions.map(s => s.hallId))];

    const requests: Promise<void>[] = [];

    filmIds.forEach(filmId => {
      if (!this.filmsCache.has(filmId)) {
        const request = new Promise<void>((resolve) => {
          this.movieService.getMovieById(filmId).subscribe({
            next: (film) => {
              if (film) {
                this.filmsCache.set(filmId, film);
              }
              resolve();
            },
            error: (error) => {
              console.error(`Ошибка загрузки фильма ${filmId}:`, error);
              resolve();
            }
          });
        });
        requests.push(request);
      }
    });

    hallIds.forEach(hallId => {
      if (!this.hallsCache.has(hallId)) {
        const request = new Promise<void>((resolve) => {
          this.hallService.getHallById(hallId).subscribe({
            next: (hall) => {
              this.hallsCache.set(hallId, hall);
              resolve();
            },
            error: (error) => {
              console.error(`Ошибка загрузки зала ${hallId}:`, error);
              resolve();
            }
          });
        });
        requests.push(request);
      }
    });

    if (requests.length === 0) {
      this.isLoading.set(false);
      this.applyFilters();
      return;
    }

    Promise.all(requests).then(() => {
      this.isLoading.set(false);
      this.applyFilters();
    });
  }

  applyFilters() {
    const currentSessions = this.sessions();
    if (currentSessions.length === 0) {
      this.filteredSessions.set([]);
      return;
    }

    let result = currentSessions.map(session => {
      const film = this.filmsCache.get(session.filmId);
      const hall = this.hallsCache.get(session.hallId);

      // Определяем, можно ли редактировать и удалять сеанс
      const canEdit = this.canEditSession(session.status);
      const canDelete = this.canDeleteSession(session.status);

      return {
        ...session,
        filmTitle: film?.title || `Фильм #${session.filmId}`,
        hallName: hall?.hallName || `Зал #${session.hallId}`,
        filmData: film,
        hallData: hall,
        localDate: new Date(session.dateTime),
        canEdit: canEdit,
        canDelete: canDelete,
        editTooltip: canEdit ? '' : this.getEditDisabledReason(session.status),
        deleteTooltip: canDelete ? '' : this.getDeleteDisabledReason(session.status)
      };
    });

    const now = new Date();
    const dateFilterValue = this.dateFilter();

    // Если задан ручной фильтр по дате, отключаем быстрые фильтры
    if (dateFilterValue) {
      // Оставляем только фильтр по дате
      result = result.filter(s =>
        this.getLocalDateString(new Date(s.dateTime)) === dateFilterValue
      );
    } else {
      // Применяем быстрые фильтры только если нет ручного фильтра по дате
      if (this.filterMode() === 'today') {
        const todayStr = this.getLocalDateString(now);
        result = result.filter(s =>
          this.getLocalDateString(new Date(s.dateTime)) === todayStr
        );
      } else if (this.filterMode() === 'week') {
        const nextWeek = new Date(now);
        nextWeek.setDate(now.getDate() + 7);
        result = result.filter(s => {
          const sessionDate = new Date(s.dateTime);
          return sessionDate >= now && sessionDate <= nextWeek;
        });
      }
    }

    // Фильтр по статусу
    const statusFilterValue = this.statusFilter();
    if (statusFilterValue) {
      result = result.filter(s => s.status === statusFilterValue);
    }

    // Фильтр по названию фильма
    const filmNameFilterValue = this.filmNameFilter();
    if (filmNameFilterValue) {
      const searchTerm = filmNameFilterValue.toLowerCase().trim();
      result = result.filter(s =>
        s.filmTitle.toLowerCase().includes(searchTerm)
      );
    }

    // Фильтр по названию зала
    const hallNameFilterValue = this.hallNameFilter();
    if (hallNameFilterValue) {
      const searchTerm = hallNameFilterValue.toLowerCase().trim();
      result = result.filter(s =>
        s.hallName.toLowerCase().includes(searchTerm)
      );
    }

    // Сортировка по дате
    result.sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime());

    this.filteredSessions.set(result);
  }

  getLocalDateString(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // Проверка, можно ли редактировать сеанс
  canEditSession(status: string): boolean {
    return !this.NON_EDITABLE_STATUSES.includes(status);
  }

  // Проверка, можно ли удалять сеанс
  canDeleteSession(status: string): boolean {
    return !this.NON_DELETABLE_STATUSES.includes(status);
  }

  // Получение причины, почему нельзя редактировать
  getEditDisabledReason(status: string): string {
    if (status === 'Активен') {
      return 'Активный сеанс нельзя редактировать';
    } else if (status === 'Завершен') {
      return 'Завершенный сеанс нельзя редактировать';
    }
    return '';
  }

  // Получение причины, почему нельзя удалять
  getDeleteDisabledReason(status: string): string {
    if (status === 'Активен') {
      return 'Активный сеанс нельзя удалять';
    }
    return '';
  }

  setFilter(mode: 'all' | 'today' | 'week') {
    this.filterMode.set(mode);
    // При выборе быстрого фильтра очищаем ручной фильтр по дате
    if (mode !== 'all') {
      this.dateFilter.set('');
    }
    this.applyFilters();
  }

  updateStatusFilter(value: string) {
    this.statusFilter.set(value);
    this.applyFilters();
  }

  updateDateFilter(value: string) {
    this.dateFilter.set(value);
    // Если выбран ручной фильтр по дате, переключаем быстрый фильтр на "Все"
    if (value) {
      this.filterMode.set('all');
    }
    this.applyFilters();
  }

  updateFilmNameFilter(value: string) {
    this.filmNameFilter.set(value);
    this.applyFilters();
  }

  updateHallNameFilter(value: string) {
    this.hallNameFilter.set(value);
    this.applyFilters();
  }

  editSession(id: number) {
    // Получаем сеанс для проверки статуса
    const session = this.sessions().find(s => s.sessionId === id);
    if (session && !this.canEditSession(session.status)) {
      alert(this.getEditDisabledReason(session.status));
      return;
    }

    this.router.navigate(['/admin/sessions/add'], {
      queryParams: { edit: true, id: id }
    });
  }

  deleteSession(id: number) {
    // Получаем сеанс для проверки статуса
    const session = this.sessions().find(s => s.sessionId === id);
    if (session && !this.canDeleteSession(session.status)) {
      alert(this.getDeleteDisabledReason(session.status));
      return;
    }

    if (!confirm('Вы уверены, что хотите удалить этот сеанс?')) return;

    this.sessionService.deleteSession(id).subscribe({
      next: () => {
        this.sessions.update(sessions =>
          sessions.filter(s => s.sessionId !== id)
        );
        this.applyFilters();
      },
      error: (error) => {
        console.error('Ошибка удаления сеанса:', error);
        alert('Не удалось удалить сеанс');
      }
    });
  }

  refreshSessions() {
    this.filmsCache.clear();
    this.hallsCache.clear();
    this.loadSessions();
  }

  clearFilters() {
    this.filterMode.set('all');
    this.dateFilter.set('');
    this.statusFilter.set('');
    this.filmNameFilter.set('');
    this.hallNameFilter.set('');
    this.applyFilters();
  }

  formatDateTime(dateTime: string): string {
    try {
      const date = new Date(dateTime);

      if (isNaN(date.getTime())) {
        return 'Некорректная дата';
      }

      return date.toLocaleString('ru-RU', {
        weekday: 'short',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      console.error('Ошибка форматирования даты:', error);
      return 'Ошибка даты';
    }
  }

  getStatusText(status: string): string {
    return status || 'Неизвестно';
  }

  getStatusClass(status: string): string {
    const statusClassMap: { [key: string]: string } = {
      'Запланирован': 'status-scheduled',
      'Активен': 'status-active',
      'Завершен': 'status-completed',
      'Отменен': 'status-cancelled'
    };
    return statusClassMap[status] || 'status-unknown';
  }
}
