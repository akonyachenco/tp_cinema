// info.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-info',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './info.html',
  styleUrls: ['./info.css']
})
export class InfoComponent implements OnInit {
  isAdmin: boolean = false;
  isAuthenticated: boolean = false;
  showScrollToTop = false;


  constructor(private authService: AuthService) {
    window.addEventListener('scroll', () => {
      this.checkScrollPosition();
    });
  }

  checkScrollPosition() {
    // Показываем кнопку, если прокрутили больше 300px
    this.showScrollToTop = window.scrollY > 300;
  }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.isAuthenticated = !!user;
    this.isAdmin = user?.role === 'ADMIN' || user?.role === 'admin';
  }

  // Методы для прокрутки к разделам
  scrollToSection(sectionId: string) {
    if (sectionId === 'top') {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    }
  }
}
