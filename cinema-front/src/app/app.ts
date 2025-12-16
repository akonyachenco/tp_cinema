// src/app/app.component.ts
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './shared/components/header/header';
import { FooterComponent } from './shared/components/footer/footer';
import { MovieCardComponent } from './shared/components/movie-card/movie-card';

import { HomeComponent } from './pages/home/home';
import { LoginComponent } from './pages/login/login';
import { RegisterComponent } from './pages/register/register';
import { AccountComponent } from './pages/account/account';
import { MovieDetailComponent } from './pages/movie-detail/movie-detail';

@Component({
  selector: 'app-root',
  standalone: true,  // ← ВАЖНО: standalone компонент!
  imports: [
    RouterOutlet,    // ← Для маршрутизации
    HeaderComponent, // ← Импортируете КАЖДЫЙ компонент здесь
    FooterComponent,
    MovieCardComponent,
    HomeComponent,
    LoginComponent,
    AccountComponent,
    RegisterComponent,
    MovieDetailComponent
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  title = 'Кинотеатр';
}
