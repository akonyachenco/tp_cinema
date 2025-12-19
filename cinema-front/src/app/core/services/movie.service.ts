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
//  private mockMovies: FilmDto[] = [
//     {
//       filmId: 1,
//       title: 'Дюна: Часть вторая',
//       description: 'Продолжение эпической саги о пустынной планете Арракис. Пол Атрейдес объединяется с Фрименами, чтобы отомстить за свою семью и предотвратить ужасное будущее.',
//       duration: 166,
//       ageRating: '16+',
//       genres: ['Фантастика', 'Драма', 'Приключения'],
//       directorId: 1,
//       countryId: 1,
//       releaseDate: '2024-03-01',
//       posterUrl: 'https://m.media-amazon.com/images/M/MV5BMDBmYTZjNjUtN2M1MS00MTQ2LTk2ODgtNzc2M2QyZGE5NTVjXkEyXkFqcGdeQXVyNzAwMjU2MTY@._V1_.jpg',
//       trailerUrl: 'https://www.youtube.com/watch?v=Way9Dexny3w'
//     },
//     {
//       filmId: 2,
//       title: 'Оппенгеймер',
//       description: 'История американского учёного Джулиуса Роберта Оппенгеймера и его роли в разработке атомной бомбы.',
//       duration: 180,
//       ageRating: '18+',
//       genres: ['Биография', 'Драма', 'Исторический'],
//       directorId: 2,
//       countryId: 1,
//       releaseDate: '2023-07-21',
//       posterUrl: 'https://m.media-amazon.com/images/M/MV5BMDBmYTZjNjUtN2M1MS00MTQ2LTk2ODgtNzc2M2QyZGE5NTVjXkEyXkFqcGdeQXVyNzAwMjU2MTY@._V1_.jpg',
//       trailerUrl: 'https://www.youtube.com/watch?v=bK6ldnjE3Y0'
//     },
//     {
//       filmId: 3,
//       title: 'Мальчик и птица',
//       description: 'Мальчик Махито открывает таинственный мир, скрытый за дверью старого дома, где встречает говорящую птицу.',
//       duration: 124,
//       ageRating: '12+',
//       genres: ['Аниме', 'Фэнтези', 'Приключения'],
//       directorId: 3,
//       countryId: 2,
//       releaseDate: '2023-07-14',
//       posterUrl: 'https://m.media-amazon.com/images/M/MV5BMDBmYTZjNjUtN2M1MS00MTQ2LTk2ODgtNzc2M2QyZGE5NTVjXkEyXkFqcGdeQXVyNzAwMjU2MTY@._V1_.jpg'
//     },
//     {
//       filmId: 4,
//       title: 'Бедные-несчастные',
//       description: 'Чёрная комедия о женщине, которая вступает в отношения с тремя разными мужчинами одновременно.',
//       duration: 141,
//       ageRating: '18+',
//       genres: ['Комедия', 'Драма', 'Романтика'],
//       directorId: 4,
//       countryId: 3,
//       releaseDate: '2023-12-22',
//       posterUrl: 'https://m.media-amazon.com/images/M/MV5BMDBmYTZjNjUtN2M1MS00MTQ2LTk2ODgtNzc2M2QyZGE5NTVjXkEyXkFqcGdeQXVyNzAwMjU2MTY@._V1_.jpg'
//     },
//     {
//       filmId: 5,
//       title: 'Годзилла и Конг: Новая империя',
//       description: 'Годзилла и Конг объединяются против скрытой угрозы, которая угрожает самому существованию их видов.',
//       duration: 115,
//       ageRating: '12+',
//       genres: ['Боевик', 'Фантастика', 'Приключения'],
//       directorId: 5,
//       countryId: 1,
//       releaseDate: '2024-03-29',
//       posterUrl: 'https://m.media-amazon.com/images/M/MV5BMDBmYTZjNjUtN2M1MS00MTQ2LTk2ODgtNzc2M2QyZGE5NTVjXkEyXkFqcGdeQXVyNzAwMjU2MTY@._V1_.jpg'
//     }
//   ];

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