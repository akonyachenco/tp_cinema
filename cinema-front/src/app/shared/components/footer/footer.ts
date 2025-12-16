// shared/footer/footer.component.ts
import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class FooterComponent {
  currentYear = new Date().getFullYear();
  
  constructor(public auth: AuthService) {}
  
  get isAuthenticated(): boolean {
    return this.auth.isAuthenticated();
  }
}