// // src/app/pages/admin-movies-add/admin-movies-add.ts
// import { Component } from '@angular/core';
// import { FormsModule } from '@angular/forms';
// import { CommonModule } from '@angular/common';
// import { RouterModule, Router } from '@angular/router';
// import { FilmDto } from '../../shared/models';
// import { MovieService } from '../../core/services/movie.service';
// import { AuthService } from '../../core/services/auth.service';

// @Component({
//   selector: 'app-admin-movies-add',
//   standalone: true,
//   imports: [CommonModule, FormsModule, RouterModule],
//   templateUrl: './admin-movies-add.html',
//   styleUrls: ['./admin-movies-add.css']
// })
// export class AdminMoviesAdd {

//   movie: FilmDto = {
//     filmId: 0,
//     title: '',
//     description: '',
//     releaseDate: new Date().toISOString().substring(0, 10),
//     duration: 120,
//     directorId: 1, // Изменил на 1 по умолчанию
//     countryId: 1,  // Изменил на 1 по умолчанию
//     ageRating: '0+',
//     genres: [],
//     sessionList: []
//   };

//   genreOptions = [
//     'action', 'comedy', 'drama', 'fantasy', 'horror', 
//     'romance', 'scifi', 'thriller', 'animation', 'adventure',
//     'documentary', 'family', 'musical', 'mystery', 'western'
//   ];

//   // Убрал загрузку файлов для упрощения
//   isSubmitting = false;
//   successMessage = '';
//   errorMessage = '';

//   constructor(
//     private movieService: MovieService,
//     private router: Router,
//     private authService: AuthService
//   ) {}

//   // Метод для навигации
//   navigateToMovies() {
//     if (this.isSubmitting) return;
//     this.router.navigate(['/admin/movies']);
//   }

//   toggleGenre(genre: string) {
//     const index = this.movie.genres.indexOf(genre);
//     if (index > -1) {
//       this.movie.genres.splice(index, 1);
//     } else {
//       this.movie.genres.push(genre);
//     }
//   }

//   addMovie() {
//     // Проверка авторизации
//     if (!this.authService.isAuthenticated()) {
//       this.errorMessage = 'Вы не авторизованы';
//       return;
//     }

//     // Проверка прав администратора
//     if (!this.authService.isAdmin()) {
//       this.errorMessage = 'Только администраторы могут добавлять фильмы';
//       return;
//     }

//     if (!this.validateForm()) {
//       return;
//     }

//     this.isSubmitting = true;
//     this.successMessage = '';
//     this.errorMessage = '';

//     // Создаем объект фильма для отправки
//     const movieData = {
//       title: this.movie.title,
//       description: this.movie.description,
//       releaseDate: this.movie.releaseDate,
//       duration: this.movie.duration,
//       directorId: this.movie.directorId,
//       countryId: this.movie.countryId,
//       ageRating: this.movie.ageRating,
//       genres: this.movie.genres
//       // posterUrl можно будет добавить позже через отдельный эндпоинт
//     };

//     console.log('Отправляемые данные:', movieData);

//     this.movieService.createMovie(movieData).subscribe({
//       next: (createdMovie) => {
//         this.isSubmitting = false;
//         this.successMessage = `Фильм "${createdMovie.title}" успешно добавлен!`;
        
//         setTimeout(() => {
//           this.router.navigate(['/admin/movies']);
//         }, 2000);
//       },
//       error: (error) => {
//         console.error('Ошибка при добавлении фильма:', error);
//         console.error('Детали ошибки:', error.error);
        
//         // Более детальные сообщения об ошибках
//         if (error.status === 403) {
//           this.errorMessage = 'Доступ запрещен. Проверьте права доступа.';
//         } else if (error.status === 401) {
//           this.errorMessage = 'Требуется авторизация. Войдите в систему.';
//         } else if (error.status === 400) {
//           this.errorMessage = 'Некорректные данные: ' + (error.error?.message || 'проверьте введенные данные');
//         } else {
//           this.errorMessage = error.error?.message || 'Не удалось добавить фильм. Код ошибки: ' + error.status;
//         }
        
//         this.isSubmitting = false;
//       }
//     });
//   }

//   validateForm(): boolean {
//     if (!this.movie.title.trim()) {
//       this.errorMessage = 'Введите название фильма';
//       return false;
//     }

//     if (!this.movie.description.trim()) {
//       this.errorMessage = 'Введите описание фильма';
//       return false;
//     }

//     if (!this.movie.releaseDate) {
//       this.errorMessage = 'Выберите дату релиза';
//       return false;
//     }

//     if (this.movie.duration < 1) {
//       this.errorMessage = 'Длительность должна быть больше 0';
//       return false;
//     }

//     if (this.movie.directorId <= 0) {
//       this.errorMessage = 'Введите корректный ID режиссера';
//       return false;
//     }

//     if (this.movie.countryId <= 0) {
//       this.errorMessage = 'Введите корректный ID страны';
//       return false;
//     }

//     this.errorMessage = '';
//     return true;
//   }

//   resetForm() {
//     if (this.isSubmitting) return;
    
//     this.movie = {
//       filmId: 0,
//       title: '',
//       description: '',
//       releaseDate: new Date().toISOString().substring(0, 10),
//       duration: 120,
//       directorId: 1,
//       countryId: 1,
//       ageRating: '0+',
//       genres: [],
//       sessionList: []
//     };
//     this.successMessage = '';
//     this.errorMessage = '';
//   }
// }

// admin-movies-add.ts
// src/app/pages/admin-movies-add/admin-movies-add.ts
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FilmDto, FilmInfoListDto, DirectorDto, CountryDto } from '../../shared/models';
import { MovieService } from '../../core/services/movie.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-movies-add',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-movies-add.html',
  styleUrls: ['./admin-movies-add.css']
})
export class AdminMoviesAdd implements OnInit {

  movie: FilmDto = {
    filmId: 0,
    title: '',
    description: '',
    releaseDate: new Date().toISOString().substring(0, 10),
    duration: 120,
    directorId: 0,
    countryId: 0,
    ageRating: '0+',
    genres: [],
    sessionList: []
  };

  // Данные из API
  directors: DirectorDto[] = [];
  countries: CountryDto[] = [];
  
  // Для управления UI
  selectedDirector: DirectorDto | null = null;
  selectedCountry: CountryDto | null = null;
  selectedDirectorCountry: CountryDto | null = null;
  
  // Режим выбора режиссера
  directorSelectionMode: 'existing' | 'new' = 'existing';
  
  // Данные для нового режиссера
  newDirectorName: string = '';
  newDirectorSurname: string = '';
  newDirectorBirthDate: string = new Date().toISOString().substring(0, 10);
  newDirectorCountryId: number = 0;

  // Поиск
  directorSearchTerm: string = '';
  countrySearchTerm: string = '';
  directorCountrySearchTerm: string = '';

  genreOptions = [
    'action', 'comedy', 'drama', 'fantasy', 'horror', 
    'romance', 'scifi', 'thriller', 'animation', 'adventure',
    'documentary', 'family', 'musical', 'mystery', 'western'
  ];

  isSubmitting = false;
  isLoading = false;
  isCreatingDirector = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private movieService: MovieService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadFilmInfo();
  }

  loadFilmInfo() {
    this.isLoading = true;
    this.errorMessage = '';

    this.movieService.getCountriesAndDirectors().subscribe({
      next: (info) => {
        this.directors = info.directors || [];
        this.countries = info.countries || [];
        this.isLoading = false;
        
        // Устанавливаем первую страну по умолчанию для фильма
        if (this.countries.length > 0 && !this.movie.countryId) {
          this.selectCountry(this.countries[0]);
        }
        
        // Устанавливаем первую страну по умолчанию для нового режиссера
        if (this.countries.length > 0 && !this.newDirectorCountryId) {
          this.selectedDirectorCountry = this.countries[0];
          this.newDirectorCountryId = this.countries[0].countryId;
        }
      },
      error: (error) => {
        console.error('Ошибка при загрузке данных:', error);
        this.errorMessage = 'Не удалось загрузить список режиссеров и стран';
        this.isLoading = false;
      }
    });
  }

  // Методы для выбора режиссера
  selectDirector(director: DirectorDto) {
    this.selectedDirector = director;
    this.movie.directorId = director.directorId;
    this.directorSelectionMode = 'existing';
  }

  selectCountry(country: CountryDto) {
    this.selectedCountry = country;
    this.movie.countryId = country.countryId;
  }

  selectDirectorCountry(country: CountryDto) {
    this.selectedDirectorCountry = country;
    this.newDirectorCountryId = country.countryId;
  }

  // Создание нового режиссера
  createNewDirector() {
    if (!this.validateNewDirector()) {
      return;
    }

    this.isCreatingDirector = true;
    this.errorMessage = '';
    const fullName = `${this.newDirectorName.trim()} ${this.newDirectorSurname.trim()}`;
    const directorData = {
      directorNameAndSurname: fullName,
      birthDate: this.newDirectorBirthDate || null,
      countryId: this.newDirectorCountryId
    };

    console.log('Создание режиссера:', directorData);

    this.movieService.createDirector(directorData).subscribe({
      next: (createdDirector) => {
        console.log('Режиссер создан:', createdDirector);
        this.isCreatingDirector = false;
        
        // Добавляем нового режиссера в список и выбираем его
        this.directors.push(createdDirector);
        this.selectDirector(createdDirector);
        
        // Переключаемся на режим существующего режиссера
        this.directorSelectionMode = 'existing';
        
        // Сбрасываем поля нового режиссера
        this.resetNewDirectorFields();
      },
      error: (error) => {
        console.error('Ошибка при создании режиссера:', error);
        this.isCreatingDirector = false;
        
        if (error.status === 400) {
          this.errorMessage = 'Некорректные данные: ' + (error.error?.message || 'проверьте введенные данные');
        } else if (error.status === 409) {
          this.errorMessage = 'Режиссер с таким именем и фамилией уже существует';
        } else {
          this.errorMessage = 'Не удалось создать режиссера: ' + (error.error?.message || 'попробуйте позже');
        }
      }
    });
  }

  // Валидация данных нового режиссера
  validateNewDirector(): boolean {
    if (!this.newDirectorName.trim()) {
      this.errorMessage = 'Введите имя режиссера';
      return false;
    }

    if (!this.newDirectorSurname.trim()) {
      this.errorMessage = 'Введите фамилию режиссера';
      return false;
    }

    if (!this.newDirectorCountryId) {
      this.errorMessage = 'Выберите страну для режиссера';
      return false;
    }

    // Проверяем, нет ли уже режиссера с таким именем и фамилией
    const fullName = `${this.newDirectorName.trim()} ${this.newDirectorSurname.trim()}`;
    const existingDirector = this.directors.find(d => 
      d.directorNameAndSurname.toLowerCase() === fullName.toLowerCase()
    );
    
    if (existingDirector) {
      this.errorMessage = 'Режиссер с таким именем и фамилией уже существует';
      return false;
    }

    return true;
  }

  // Сброс полей нового режиссера
  resetNewDirectorFields() {
    this.newDirectorName = '';
    this.newDirectorSurname = '';
    this.newDirectorBirthDate = new Date().toISOString().substring(0, 10);
    
    if (this.countries.length > 0) {
      this.selectedDirectorCountry = this.countries[0];
      this.newDirectorCountryId = this.countries[0].countryId;
    } else {
      this.selectedDirectorCountry = null;
      this.newDirectorCountryId = 0;
    }
  }

  // Получение фильтрованных списков для поиска
  get filteredDirectors(): DirectorDto[] {
    if (!this.directorSearchTerm.trim()) {
      return this.directors;
    }
    
    const searchTerm = this.directorSearchTerm.toLowerCase();
    return this.directors.filter(director =>
      director.directorNameAndSurname.toLowerCase().includes(searchTerm) 
    );
  }

  get filteredCountries(): CountryDto[] {
    if (!this.countrySearchTerm.trim()) {
      return this.countries;
    }
    
    const searchTerm = this.countrySearchTerm.toLowerCase();
    return this.countries.filter(country =>
      country.countryName.toLowerCase().includes(searchTerm)
    );
  }

  get filteredDirectorCountries(): CountryDto[] {
    if (!this.directorCountrySearchTerm.trim()) {
      return this.countries;
    }
    
    const searchTerm = this.directorCountrySearchTerm.toLowerCase();
    return this.countries.filter(country =>
      country.countryName.toLowerCase().includes(searchTerm)
    );
  }

  // Метод для навигации
  navigateToMovies() {
    if (this.isSubmitting) return;
    this.router.navigate(['/admin/movies']);
  }

  toggleGenre(genre: string) {
    const index = this.movie.genres.indexOf(genre);
    if (index > -1) {
      this.movie.genres.splice(index, 1);
    } else {
      this.movie.genres.push(genre);
    }
  }

  addMovie() {
    // Проверка авторизации
    if (!this.authService.isAuthenticated()) {
      this.errorMessage = 'Вы не авторизованы';
      return;
    }

    // Проверка прав администратора
    if (!this.authService.isAdmin()) {
      this.errorMessage = 'Только администраторы могут добавлять фильмы';
      return;
    }

    if (!this.validateForm()) {
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    // Создаем объект фильма для отправки
    const movieData = {
      title: this.movie.title,
      description: this.movie.description,
      releaseDate: this.movie.releaseDate,
      duration: this.movie.duration,
      directorId: this.movie.directorId,
      countryId: this.movie.countryId,
      ageRating: this.movie.ageRating,
      genres: this.movie.genres
    };

    console.log('Отправляемые данные фильма:', movieData);

    this.movieService.createMovie(movieData).subscribe({
      next: (createdMovie) => {
        this.isSubmitting = false;
        this.successMessage = `Фильм "${createdMovie.title}" успешно добавлен!`;
        
        setTimeout(() => {
          this.router.navigate(['/admin/movies']);
        }, 2000);
      },
      error: (error) => {
        console.error('Ошибка при добавлении фильма:', error);
        console.error('Детали ошибки:', error.error);
        
        // Более детальные сообщения об ошибках
        if (error.status === 403) {
          this.errorMessage = 'Доступ запрещен. Проверьте права доступа.';
        } else if (error.status === 401) {
          this.errorMessage = 'Требуется авторизация. Войдите в систему.';
        } else if (error.status === 400) {
          this.errorMessage = 'Некорректные данные: ' + (error.error?.message || 'проверьте введенные данные');
        } else {
          this.errorMessage = error.error?.message || 'Не удалось добавить фильм. Код ошибки: ' + error.status;
        }
        
        this.isSubmitting = false;
      }
    });
  }

  validateForm(): boolean {
    // Основные проверки
    if (!this.movie.title.trim()) {
      this.errorMessage = 'Введите название фильма';
      return false;
    }

    if (!this.movie.description.trim()) {
      this.errorMessage = 'Введите описание фильма';
      return false;
    }

    if (!this.movie.releaseDate) {
      this.errorMessage = 'Выберите дату релиза';
      return false;
    }

    if (this.movie.duration < 1) {
      this.errorMessage = 'Длительность должна быть больше 0';
      return false;
    }

    // Проверка режиссера
    if (!this.movie.directorId) {
      this.errorMessage = 'Выберите или создайте режиссера';
      return false;
    }

    // Проверка страны фильма
    if (!this.movie.countryId) {
      this.errorMessage = 'Выберите страну производства фильма';
      return false;
    }

    this.errorMessage = '';
    return true;
  }

  resetForm() {
    if (this.isSubmitting) return;
    
    this.movie = {
      filmId: 0,
      title: '',
      description: '',
      releaseDate: new Date().toISOString().substring(0, 10),
      duration: 120,
      directorId: 0,
      countryId: 0,
      ageRating: '0+',
      genres: [],
      sessionList: []
    };
    
    this.selectedDirector = null;
    this.selectedCountry = null;
    this.selectedDirectorCountry = null;
    this.directorSelectionMode = 'existing';
    this.resetNewDirectorFields();
    this.directorSearchTerm = '';
    this.countrySearchTerm = '';
    this.directorCountrySearchTerm = '';
    this.successMessage = '';
    this.errorMessage = '';
    this.isCreatingDirector = false;
  }
}