import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FilmDto } from '../../models'

@Component({
  selector: 'app-movie-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './movie-card.html',
  styleUrls: ['./movie-card.css'] // добавьте этот файл
})
export class MovieCardComponent {
  @Input() movie!: FilmDto;

  // Обработчик ошибки загрузки изображения
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    img.parentElement!.innerHTML = '<div class="poster-placeholder">🎬</div>';
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
}