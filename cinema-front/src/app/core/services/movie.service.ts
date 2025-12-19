// src/app/core/services/movie.service.ts
import { Injectable } from '@angular/core';
import { Observable,of } from 'rxjs';
import { ApiService } from './api.service';
import { DirectorDto, FilmDto } from '../../shared/models';
import { FilmInfoListDto } from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class MovieService {
    constructor(private api: ApiService) {}


  getAllMovies(): Observable<FilmDto[]> {
    // return of(this.mockMovies);
     return this.api.get<FilmDto[]>('films');
  }

  getCountriesAndDirectors(): Observable<FilmInfoListDto>{
    return this.api.get<FilmInfoListDto>('films/info')
  }

  createDirector(director: any): Observable<DirectorDto>{
    return this.api.post<DirectorDto>('films/director', director);
  }

  getMovieById(id: number): Observable<FilmDto | undefined> {
    // const movie = this.mockMovies.find(m => m.filmId === id);
    // return of(movie);
    return this.api.get<FilmDto>(`films/${id}`);
  }

  // Получить активные фильмы (с сеансами)
  getActiveMovies(): Observable<FilmDto[]> {
    return this.api.get<FilmDto[]>('films/active');
  }

  // Создать фильм (админ)
  createMovie(movie: any): Observable<FilmDto> {
    return this.api.post<FilmDto>('films', movie);
  }
  // Обновить фильм (админ)
  updateMovie(id: number, movie: FilmDto): Observable<FilmDto> {
    return this.api.put<FilmDto>(`films/${id}`, movie);
  }

  // Удалить фильм (админ)
  deleteMovie(id: number): Observable<void> {
    return this.api.delete<void>(`films/${id}`);
  }

  // Поиск фильмов
  searchMovies(query: string): Observable<FilmDto[]> {
    return this.api.get<FilmDto[]>(`films/search?query=${query}`);
  }

  // Получить фильмы по жанру
  getMoviesByGenre(genre: string): Observable<FilmDto[]> {
    return this.api.get<FilmDto[]>(`films/genre/${genre}`);
  }
}
