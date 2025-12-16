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


export const routes: Routes = [  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'account', component: AccountComponent },
  { path: 'movie-card', component: MovieCardComponent},
  { path: 'movie/:id', component: MovieDetailComponent },
  { path: 'ticket/:id', component: TicketComponent }, // по ID билета
  { path: 'booking/:bookingId/ticket', component: TicketComponent }, // по ID брони
  { path: 'ticket', component: TicketComponent }, // демо-страница
  { path: 'session/:id', component: SessionComponent },
  { path: 'confirm', component: ConfirmComponent },
  { path: '**', redirectTo: '' }
];
