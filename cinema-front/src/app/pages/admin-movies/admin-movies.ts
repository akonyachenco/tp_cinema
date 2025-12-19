// src/app/pages/admin-movies/admin-movies.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FilmDto } from '../../shared/models';
import { MovieService } from '../../core/services/movie.service';

@Component({
  selector: 'app-admin-movies',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-movies.html',
  styleUrls: ['./admin-movies.css']
})
export class AdminMovies implements OnInit {

  movies: FilmDto[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(private movieService: MovieService) {}

  ngOnInit() {
    this.loadMovies();
  }

  loadMovies() {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.movieService.getAllMovies().subscribe({
      next: (movies) => {
        this.movies = movies;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка при загрузке фильмов:', error);
        this.errorMessage = 'Не удалось загрузить список фильмов. Попробуйте позже.';
        this.isLoading = false;
      }
    });
  }

  deleteMovie(filmId: number) {
    if (!confirm('Удалить этот фильм?')) return;
    
    this.movieService.deleteMovie(filmId).subscribe({
      next: () => {
        this.movies = this.movies.filter(f => f.filmId !== filmId);
      },
      error: (error) => {
        console.error('Ошибка при удалении фильма:', error);
        alert('Не удалось удалить фильм. Возможно, есть связанные сеансы.');
      }
    });
  }

  refreshMovies() {
    this.loadMovies();
  }
}