// pages/home/home.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MovieCardComponent } from '../../shared/components/movie-card/movie-card';
import { MovieService } from '../../core/services/movie.service';
import { FilmDto } from '../../shared/models';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
    imports: [
    CommonModule,
    MovieCardComponent 
  ]
})
export class HomeComponent implements OnInit {
  movies: FilmDto[] = [];
  filteredMovies: FilmDto[] = [];
  isLoading = true;
  activeFilter: 'today' | 'soon' | 'all' = 'today';

  constructor(private movieService: MovieService) {}

  ngOnInit(): void {
    this.loadMovies();
  }

  loadMovies(): void {
    this.isLoading = true;
    this.movieService.getAllMovies().subscribe({
      next: (movies) => {
        this.movies = movies;
        this.filteredMovies = movies; // По умолчанию все
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка загрузки фильмов:', error);
        this.isLoading = false;
      }
    });
  }

  setFilter(filter: 'today' | 'soon' | 'all'): void { // ← ДОБАВИТЬ 'all'
    this.activeFilter = filter;
    
    // Простая логика фильтрации для примера:
    switch(filter) {
      case 'today':
        // Показываем фильмы с сегодняшней датой (пока все)
        this.filteredMovies = this.movies.slice(0, 3);
        break;
      case 'soon':
        // Показываем фильмы "скоро" (остальные)
        this.filteredMovies = this.movies.slice(3);
        break;
      case 'all':
        // Показываем все фильмы
        this.filteredMovies = this.movies;
        break;
    }
  }
}