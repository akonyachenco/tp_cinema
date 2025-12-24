// admin-directors.ts
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { DirectorDto, CountryDto } from '../../shared/models';
import { MovieService } from '../../core/services/movie.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-directors',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-directors.html',
  styleUrls: ['./admin-directors.css']
})
export class AdminDirectors implements OnInit {
  // Списки данных
  directors: DirectorDto[] = [];
  countries: CountryDto[] = [];
  
  // Поиск
  directorSearchTerm: string = '';
  directorCountrySearchTerm: string = '';
  
  // Выбранные элементы
  selectedDirectorId: number | null = null;
  selectedDirectorCountry: CountryDto | null = null;
  
  // Данные для нового режиссера
  newDirectorName: string = '';
  newDirectorSurname: string = '';
  newDirectorBirthDate: string = new Date().toISOString().substring(0, 10);
  newDirectorCountryId: number = 0;
  
  // Состояние UI
  showCreateForm: boolean = false;
  isCreatingDirector: boolean = false;
  isLoading: boolean = false;
  isEditing: boolean = false;
  editingDirectorId: number | null = null;
  
  // Сообщения
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private movieService: MovieService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadDirectorsAndCountries();
  }
hasFilms(director: DirectorDto): boolean {
  return !!director.filmList && director.filmList.length > 0;
}
  loadDirectorsAndCountries() {
    this.isLoading = true;
    this.errorMessage = '';

    this.movieService.getCountriesAndDirectors().subscribe({
      next: (info) => {
        this.directors = info.directors || [];
        this.countries = info.countries || [];
        this.isLoading = false;

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

  // Фильтрация списков
  get filteredDirectors(): DirectorDto[] {
    if (!this.directorSearchTerm.trim()) {
      return this.directors;
    }

    const searchTerm = this.directorSearchTerm.toLowerCase();
    return this.directors.filter(director =>
      director.directorNameAndSurname.toLowerCase().includes(searchTerm)
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

  // Выбор элементов
  selectDirector(director: DirectorDto) {
    this.selectedDirectorId = director.directorId;
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

        // Добавляем нового режиссера в список
        this.directors.push(createdDirector);
        
        // Сбрасываем форму
        this.resetNewDirectorForm();
        this.showCreateForm = false;
        
        // Показываем сообщение об успехе
        this.successMessage = `Режиссер "${createdDirector.directorNameAndSurname}" успешно добавлен!`;
        this.errorMessage = '';
        
        // Автоматически скрываем сообщение через 3 секунды
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        console.error('Ошибка при создании режиссера:', error);
        this.isCreatingDirector = false;

        if (error.status === 400) {
          this.errorMessage = 'Некорректные данные: ' + (error.error?.message || 'проверьте введенные данные');
        } else if (error.status === 409) {
          this.errorMessage = 'Режиссер с таким именем и фамилией уже существует';
        } else if (error.status === 403) {
          this.errorMessage = 'Доступ запрещен. Проверьте права администратора.';
        } else if (error.status === 401) {
          this.errorMessage = 'Требуется авторизация. Войдите в систему.';
        } else {
          this.errorMessage = 'Не удалось создать режиссера: ' + (error.error?.message || 'попробуйте позже');
        }
        
        this.successMessage = '';
      }
    });
  }

  // Валидация нового режиссера
  validateNewDirector(): boolean {
    // Проверка авторизации
    if (!this.authService.isAuthenticated()) {
      this.errorMessage = 'Вы не авторизованы';
      return false;
    }

    // Проверка прав администратора
    if (!this.authService.isAdmin()) {
      this.errorMessage = 'Только администраторы могут добавлять режиссеров';
      return false;
    }

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

    this.errorMessage = '';
    return true;
  }

  // Редактирование режиссера (заглушка для будущей реализации)
  editDirector(director: DirectorDto) {
    this.isEditing = true;
    this.editingDirectorId = director.directorId;
    
    // Заполняем форму данными режиссера для редактирования
    this.newDirectorName = this.extractFirstName(director.directorNameAndSurname);
    this.newDirectorSurname = this.extractLastName(director.directorNameAndSurname);
    this.newDirectorBirthDate = director.birthDate || new Date().toISOString().substring(0, 10);
    
    // Находим страну режиссера
    const country = this.countries.find(c => c.countryId === director.countryId);
    if (country) {
      this.selectedDirectorCountry = country;
      this.newDirectorCountryId = country.countryId;
    }
    
    this.showCreateForm = true;
    this.successMessage = `Редактирование режиссера: ${director.directorNameAndSurname}`;
  }

  // Удаление режиссера (заглушка для будущей реализации)
  deleteDirector(directorId: number) {
    const director = this.directors.find(d => d.directorId === directorId);
    if (!director) return;

    if (director.filmList && director.filmList.length > 0) {
      this.errorMessage = `Нельзя удалить режиссера "${director.directorNameAndSurname}", так как у него есть фильмы (${director.filmList.length})`;
      return;
    }

    if (confirm(`Вы уверены, что хотите удалить режиссера "${director.directorNameAndSurname}"?`)) {
      // В будущем здесь будет вызов API для удаления
      console.log('Удаление режиссера:', directorId);
      this.successMessage = `Функция удаления режиссера "${director.directorNameAndSurname}" будет реализована в будущем`;
      
      setTimeout(() => {
        this.successMessage = '';
      }, 3000);
    }
  }

  // Вспомогательные методы
  extractFirstName(fullName: string): string {
    const parts = fullName.split(' ');
    return parts[0] || '';
  }

  extractLastName(fullName: string): string {
    const parts = fullName.split(' ');
    return parts.slice(1).join(' ') || '';
  }

  getCountryName(countryId: number): string {
    const country = this.countries.find(c => c.countryId === countryId);
    return country ? country.countryName : 'Не указана';
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'Не указана';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
  }

  // Статистика
  get directorsWithMoviesCount(): number {
    return this.directors.filter(d => d.filmList && d.filmList.length > 0).length;
  }

  get directorsWithoutMoviesCount(): number {
    return this.directors.filter(d => !d.filmList || d.filmList.length === 0).length;
  }

  // Управление UI
  toggleCreateDirectorForm() {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.resetNewDirectorForm();
      this.isEditing = false;
      this.editingDirectorId = null;
      this.successMessage = '';
    }
  }

  resetNewDirectorForm() {
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
    
    this.directorCountrySearchTerm = '';
    this.isEditing = false;
    this.editingDirectorId = null;
  }

  onSearchChange() {
    // Можно добавить дополнительную логику при поиске
  }

  // Навигация
  navigateToMovies() {
    this.router.navigate(['/admin/movies']);
  }
}