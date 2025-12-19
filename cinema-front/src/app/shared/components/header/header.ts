// shared/header/header.component.ts
import { Component } from '@angular/core';
import { Router,  RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
    imports: [RouterModule, CommonModule],
  styleUrl: './header.css'
})
export class HeaderComponent {
  appName = 'Кинотеатр';
  isAuthenticated = false;
  isAdmin = false;
  isUser = false;

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Подписываемся на изменения пользователя
    this.auth.currentUser$.subscribe(user => {
      console.log('Пользователь изменился:', user);
      this.isAuthenticated = !!user;
      this.isAdmin = user?.role?.toLowerCase() === 'admin';
      this.isUser = user?.role?.toLowerCase() === 'user';
    });
  }

  logout(): void {
    this.auth.logout();
  }
  
    get isUserVisible(): boolean {
    return this.isUser;
  }
  // constructor(
  //   public auth: AuthService,
  //   private router: Router
  // ) {}

  // get isAuthenticated(): boolean {
  //   return this.auth.isAuthenticated();
  // }

  // get isAdmin(): boolean {
  //   return this.auth.isAdmin();
  // }

  // logout(): void {
  //   this.auth.logout();
  //   this.router.navigate(['/']);
  // }
}