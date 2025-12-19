// src/app/pages/admin-halls/admin-halls.ts
import { Component, OnInit } from '@angular/core';
import { RouterModule, Router } from '@angular/router'; // Добавляем Router
import { CommonModule } from '@angular/common';
import { HallDto } from '../../shared/models/hall.model';
import { HallService } from '../../core/services/hall.service';

@Component({
  selector: 'app-admin-halls',
  templateUrl: './admin-halls.html',
  styleUrls: ['./admin-halls.css'],
  standalone: true,
  imports: [RouterModule, CommonModule]
})
export class AdminHalls implements OnInit {

  halls: HallDto[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private hallService: HallService,
    private router: Router // Добавляем Router
  ) {}

  ngOnInit() {
    this.loadHalls();
  }

  loadHalls() {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.hallService.getAllHalls().subscribe({
      next: (halls) => {
        this.halls = halls;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка при загрузке залов:', error);
        this.errorMessage = 'Не удалось загрузить список залов. Попробуйте позже.';
        this.isLoading = false;
      }
    });
  }

  deleteHall(hallId: number) {
    if (!confirm('Удалить этот зал?')) return;
    
    this.hallService.deleteHall(hallId).subscribe({
      next: () => {
        this.halls = this.halls.filter(h => h.hallId !== hallId);
      },
      error: (error) => {
        console.error('Ошибка при удалении зала:', error);
        alert('Не удалось удалить зал. Возможно, есть связанные сеансы.');
      }
    });
  }

  editHall(hallId: number) {
    // Переход на страницу конструктора залов с предварительно выбранным залом
    this.router.navigate(['/admin/hall-constructor'], {
      queryParams: { 
        editHallId: hallId 
      }
    });
  }

  refreshHalls() {
    this.loadHalls();
  }
}