import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { LoginComponent } from './pages/login/login';
import { RegisterComponent } from './pages/register/register';
import { AccountComponent } from './pages/account/account';
import { TicketComponent } from './pages/ticket/ticket';
import { MovieCardComponent } from './shared/components/movie-card/movie-card';
import { MovieDetailComponent } from './pages/movie-detail/movie-detail';
import { SessionComponent } from './pages/session/session';
import { ConfirmComponent } from './pages/confirm/confirm';

import { AdminPanel } from './pages/admin-panel/admin-panel';
import { AdminHalls } from './pages/admin-halls/admin-halls';
import { AdminHallConstructor } from './pages/admin-hall-constructor/admin-hall-constructor';
import { AdminMovies } from './pages/admin-movies/admin-movies';
import { AdminMoviesAdd } from './pages/admin-movies-add/admin-movies-add';
import { AdminSessions } from './pages/admin-sessions/admin-sessions';
import { AdminSessionAdd } from './pages/admin-session-add/admin-session-add';

export const routes: Routes = [  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'account', component: AccountComponent },
  { path: 'movie-card', component: MovieCardComponent},
  { path: 'movie/:id', component: MovieDetailComponent },
  { path: 'ticket/:id', component: TicketComponent }, // по ID билета
  { path: 'booking/:bookingId/ticket', component: TicketComponent },
  { path: 'session/:id', component: SessionComponent },
  { path: 'confirm', component: ConfirmComponent },
  { path: 'admin',component: AdminPanel},
  { path: 'admin/halls', component: AdminHalls },
  { path: 'admin/hall-constructor', component: AdminHallConstructor },
  { path: 'admin/halls/edit/:id', component: AdminHalls }, 
  { path: 'admin/movies', component: AdminMovies },
  { path: 'admin/movies/add', component: AdminMoviesAdd },
  { path: 'admin/sessions', component: AdminSessions },
  { path: 'admin/sessions/add', component: AdminSessionAdd },
  { path: 'admin/sessions/:filmId', component: AdminSessions },

  { path: '**', redirectTo: '' }
];


