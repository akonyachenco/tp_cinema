// src/app/pages/register/register.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(40)]],
      surname: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(40)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      birthDate: ['', [Validators.required, this.validateBirthDate]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  // Валидатор для даты рождения (не будущее, старше 18 лет)
  validateBirthDate(control: AbstractControl): { [key: string]: boolean } | null {
    if (!control.value) return null;
    
    const birthDate = new Date(control.value);
    const today = new Date();
    const minAgeDate = new Date();
    minAgeDate.setFullYear(today.getFullYear() - 18);
    
    if (birthDate > today) {
      return { futureDate: true };
    }
    
    if (birthDate > minAgeDate) {
      return { underage: true };
    }
    
    return null;
  }

  // Валидатор совпадения паролей
  passwordMatchValidator(form: AbstractControl): { [key: string]: boolean } | null {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    
    if (password !== confirmPassword) {
      return { passwordMismatch: true };
    }
    
    return null;
  }
  get maxBirthDate(): string {
  const today = new Date();
  return today.toISOString().split('T')[0];
}

  onSubmit(): void {
    if (this.registerForm.invalid) {
      // Помечаем все поля как touched для показа ошибок
      Object.keys(this.registerForm.controls).forEach(key => {
        const control = this.registerForm.get(key);
        control?.markAsTouched();
      });
      return;
    }
    
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    const formData = {
      name: this.registerForm.value.name,
      surname: this.registerForm.value.surname,
      email: this.registerForm.value.email,
      birthDate: this.registerForm.value.birthDate,
      password: this.registerForm.value.password
    };
    
    console.log('Регистрация с данными:', formData);
    
    this.auth.register(formData).subscribe({
      next: (response) => {
        console.log('Успешная регистрация:', response);
        this.successMessage = 'Регистрация успешна! Перенаправляем...';
        
        setTimeout(() => {
          this.router.navigate(['/account']);
        }, 2000);
      },
      error: (error) => {
        console.error('Ошибка регистрации:', error);
        this.errorMessage = error.error?.message || 'Ошибка регистрации. Попробуйте позже.';
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  // Геттеры для удобства в шаблоне
  get name() { return this.registerForm.get('name'); }
  get surname() { return this.registerForm.get('surname'); }
  get email() { return this.registerForm.get('email'); }
  get birthDate() { return this.registerForm.get('birthDate'); }
  get password() { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }
}