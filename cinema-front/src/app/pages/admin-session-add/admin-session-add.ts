// admin-session-add.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { MovieService } from '../../core/services/movie.service';
import { HallService } from '../../core/services/hall.service';
import { SessionService } from '../../core/services/session.service';
import { FilmDto, HallDto, CreateSessionRequest, SessionDto } from '../../shared/models';

@Component({
  selector: 'app-admin-session-add',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-session-add.html',
  styleUrls: ['./admin-session-add.css']
})
export class AdminSessionAdd implements OnInit {
  sessionForm!: FormGroup;
  movies: FilmDto[] = [];
  halls: HallDto[] = [];
  today: string = '';
  
  // Режим редактирования
  isEditMode = false;
  sessionId: number | null = null;
  originalSession: SessionDto | null = null;
  
  isLoading = false;
  isLoadingData = false;
  errorMessage = '';
  filmIdFromRoute: number | null = null;

  constructor(
    private fb: FormBuilder, 
    private router: Router,
    private route: ActivatedRoute,
    private movieService: MovieService,
    private hallService: HallService,
    private sessionService: SessionService
  ) {}

  ngOnInit(): void {
    this.today = new Date().toISOString().split('T')[0];
    
    // Получаем параметры из URL
    this.route.params.subscribe(params => {
      if (params['filmId']) {
        this.filmIdFromRoute = +params['filmId'];
      }
    });
    
    // Проверяем режим редактирования через query параметры
    this.route.queryParams.subscribe(params => {
      if (params['edit'] && params['id']) {
        this.isEditMode = true;
        this.sessionId = +params['id'];
        this.loadSessionForEdit(this.sessionId);
      } else {
        this.isEditMode = false;
        this.initializeForm();
        this.loadMoviesAndHalls();
      }
    });
  }

  initializeForm() {
    this.sessionForm = this.fb.group({
      filmId: [this.filmIdFromRoute || '', Validators.required],
      hallId: ['', Validators.required],
      session_date: ['', Validators.required],
      session_time: ['', Validators.required],
      status: ['active', Validators.required],
      additional_info: ['']
    });
  }

  loadMoviesAndHalls() {
    this.isLoadingData = true;
    
    // Загружаем фильмы
    this.movieService.getAllMovies().subscribe({
      next: (movies) => {
        this.movies = movies;
        this.isLoadingData = false;
      },
      error: (error) => {
        console.error('Ошибка загрузки фильмов:', error);
        this.errorMessage = 'Не удалось загрузить список фильмов';
        this.isLoadingData = false;
      }
    });
    
    // Загружаем залы
    this.hallService.getAllHalls().subscribe({
      next: (halls) => {
        this.halls = halls;
      },
      error: (error) => {
        console.error('Ошибка загрузки залов:', error);
        if (!this.errorMessage) {
          this.errorMessage = 'Не удалось загрузить список залов';
        }
      }
    });
  }

  loadSessionForEdit(sessionId: number) {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.sessionService.getSessionById(sessionId).subscribe({
      next: (session) => {
        this.originalSession = session;
        this.initializeEditForm(session);
        this.loadMoviesAndHalls();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка загрузки сеанса для редактирования:', error);
        this.errorMessage = 'Не удалось загрузить данные сеанса';
        this.isLoading = false;
      }
    });
  }

  initializeEditForm(session: SessionDto) {
    const dateTime = new Date(session.dateTime);
    const session_date = dateTime.toISOString().split('T')[0];
    const session_time = dateTime.toTimeString().slice(0, 5);
    
    this.sessionForm = this.fb.group({
      filmId: [session.filmId, Validators.required],
      hallId: [session.hallId, Validators.required],
      session_date: [session_date, Validators.required],
      session_time: [session_time, Validators.required],
      status: [session.status || 'active', Validators.required],
      additional_info: ['']
    });
  }

  getMovieTitle(filmId: number): string {
    const movie = this.movies.find(m => m.filmId === filmId);
    return movie ? movie.title : `Фильм #${filmId}`;
  }

  getHallName(hallId: number): string {
    const hall = this.halls.find(h => h.hallId === hallId);
    return hall ? hall.hallName : `Зал #${hallId}`;
  }

  onSubmit() {
    if (this.sessionForm.invalid) {
      this.markFormGroupTouched(this.sessionForm);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const form = this.sessionForm.value;
    const dateTime = `${form.session_date}T${form.session_time}`;

    const sessionRequest: CreateSessionRequest = {
      filmId: +form.filmId,
      hallId: +form.hallId,
      dateTime: dateTime,
      status: form.status
    };

    if (this.isEditMode && this.sessionId) {
      // Обновление существующего сеанса
      const updateData: SessionDto = {
        ...this.originalSession!,
        filmId: +form.filmId,
        hallId: +form.hallId,
        dateTime: dateTime,
        status: form.status
      };

      this.sessionService.updateSession(this.sessionId, updateData).subscribe({
        next: (updatedSession) => {
          console.log('Сеанс обновлен:', updatedSession);
          alert('Сеанс успешно обновлен!');
          this.router.navigate(['/admin/sessions']);
        },
        error: (error) => {
          console.error('Ошибка обновления сеанса:', error);
          this.errorMessage = 'Не удалось обновить сеанс';
          this.isLoading = false;
        }
      });
    } else {
      // Создание нового сеанса
      this.sessionService.createSession(sessionRequest).subscribe({
        next: (createdSession) => {
          console.log('Сеанс создан:', createdSession);
          alert('Сеанс успешно создан!');
          this.router.navigate(['/admin/sessions']);
        },
        error: (error) => {
          console.error('Ошибка создания сеанса:', error);
          this.errorMessage = 'Не удалось создать сеанс';
          this.isLoading = false;
        }
      });
    }
  }

  // Вспомогательный метод для валидации формы
  markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  // Получить сообщение об ошибке для поля
  getFieldError(fieldName: string): string {
    const field = this.sessionForm.get(fieldName);
    
    if (!field || !field.errors || !field.touched) {
      return '';
    }
    
    if (field.errors['required']) {
      return 'Это поле обязательно для заполнения';
    }
    
    return 'Неверное значение';
  }

  // Проверить, валидно ли поле
  isFieldValid(fieldName: string): boolean {
    const field = this.sessionForm.get(fieldName);
    return !field || (!field.invalid || !field.touched);
  }
}