import { Component, Input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FilmDto, SessionDto } from '../../models'
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-movie-card',
  templateUrl: './movie-card.html',
  styleUrls: ['./movie-card.css'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    DatePipe
  ]
})
export class MovieCardComponent {
  
  @Input() movie!: FilmDto;
  @Input() showSessions: boolean = false;
  @Input() sessionDates: {date: string, sessions: SessionDto[]}[] = [];
  // Изменяем тип на 'week'
  @Input() activeFilter: 'today' | 'tomorrow' | 'week' | 'all' = 'all';
  isAdmin: boolean = false;
  // Обработчик ошибки загрузки изображения
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    img.parentElement!.innerHTML = '<div class="poster-placeholder">🎬</div>';
  }
  constructor(private authService: AuthService) {}
  ngOnInit(): void {
    // Проверяем роль пользователя при инициализации
    this.checkAdminStatus();
  }

  // Проверка, является ли пользователь администратором
  checkAdminStatus(): void {
    const user = this.authService.getCurrentUser();
    this.isAdmin = user?.role === 'ADMIN' || user?.role === 'admin';
  }
    onSessionClick(event: Event, session: SessionDto): void {
    if (this.isAdmin) {
      // Для администратора блокируем переход и показываем сообщение
      event.preventDefault();
      alert('Администраторы не могут бронировать билеты. Перейдите в административную панель для управления сеансами.');
    }
  }
  // Получить год из даты
  getYear(dateString: string | undefined): string {
    if (!dateString) return '2024';
    return dateString.split('-')[0];
  }

  // Обрезать описание
  truncateDescription(description: string): string {
    if (description.length > 100) {
      return description.substring(0, 100) + '...';
    }
    return description;
  }

  // Форматировать время сеанса
  formatSessionTime(dateTime: string): string {
    try {
      const date = new Date(dateTime);
      return date.toLocaleTimeString('ru-RU', {
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return '--:--';
    }
  }

  // Проверяет, есть ли сеансы для отображения
  get hasSessions(): boolean {
    return this.showSessions && this.sessionDates && this.sessionDates.length > 0;
  }
}
