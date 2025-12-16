// pages/login/login.component.ts
import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router'; 
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-login',
  templateUrl: './login.html',
   imports: [
    ReactiveFormsModule, // ← ДОБАВИТЬ для formGroup
    CommonModule,
    RouterModule
  ],
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;
    
    this.isLoading = true;
    this.errorMessage = '';
    
    this.auth.login(this.loginForm.value).subscribe({
      next: () => {
        this.router.navigate(['/account']);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Ошибка входа';
        this.isLoading = false;
      }
    });
  }
}