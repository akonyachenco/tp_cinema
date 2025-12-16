// src/app/core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ApiService } from './api.service';
import { UserLoginDto, UserRegisterDto, UserDto } from '../../shared/models';


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<UserDto | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private api: ApiService, private router: Router) {
    // Проверяем сохранённого пользователя при старте
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      this.currentUserSubject.next(JSON.parse(savedUser));
    }
  }

  // Вход
  login(credentials: UserLoginDto): Observable<any> {
        const mockUser = {
userId: 1,
    name: 'Тестовый',
    surname: 'Пользователь', // ← добавить
    email: credentials.email,
    birthDate: '1990-01-01', // ← добавить
    age: 30, // ← добавить
    registrationDate: new Date().toISOString().split('T')[0], // ← добавить
    role: 'user'
    };
    
    localStorage.setItem('currentUser', JSON.stringify(mockUser));
    localStorage.setItem('token', 'mock-jwt-token');
    this.currentUserSubject.next(mockUser);
    
    return of({ success: true, user: mockUser });
    // return new Observable(observer => {
    //   this.api.post('auth/login', credentials).subscribe({
    //     next: (response: any) => {
    //       const user: UserDto = response.user; // Предполагаем, что бэкенд возвращает user и token
    //       const token = response.token;
          
    //       localStorage.setItem('currentUser', JSON.stringify(user));
    //       localStorage.setItem('token', token);
    //       this.currentUserSubject.next(user);
          
    //       observer.next(response);
    //       observer.complete();
    //     },
    //     error: (error) => observer.error(error)
    //   });
    // });
  }

  // Регистрация
  register(userData: UserRegisterDto): Observable<any> {
     return of({ success: true, message: 'Регистрация успешна' });
    // return this.api.post('auth/register', userData);
  }

  // Выход
  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  // Получить текущего пользователя
  getCurrentUser(): UserDto | null {
    return this.currentUserSubject.value;
  }

  // Проверка авторизации
  isAuthenticated(): boolean {
    return !!this.currentUserSubject.value;
  }

  // Проверка роли администратора
  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === 'ADMIN' || user?.role === 'admin';
  }

  // Получить токен
  getToken(): string | null {
    return localStorage.getItem('token');
  }
}