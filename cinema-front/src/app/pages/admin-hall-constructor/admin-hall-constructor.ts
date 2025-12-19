// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { HallService } from '../../core/services/hall.service';
// import { AuthService} from '../../core/services/auth.service';
// import { HallDto, SeatDto } from '../../shared/models';

// @Component({
//   selector: 'app-admin-hall-constructor',
//   standalone: true,
//   imports: [CommonModule, FormsModule],
//   templateUrl: './admin-hall-constructor.html',
//   styleUrls: ['./admin-hall-constructor.css']
// })
// export class AdminHallConstructor implements OnInit {

//   halls: HallDto[] = [];
//   selectedHallId: number | null = null;
//   hallName = '';
//   hallType = '2D';
//   basePrice = 300;
//   activeTool: 'select' | 'vip' | 'block' = 'select';
//   isLoading = false;

//   // layout: двумерный массив мест
//   layout: SeatDto[][] = [];

//   constructor(
//     private hallService: HallService,
//     private authService: AuthService
//   ) {}

//   ngOnInit() {
//     this.loadHalls();
//   }

//   loadHalls(): void {
//     this.isLoading = true;
//     this.hallService.getAllHalls().subscribe({
//       next: (halls) => {
//         this.halls = halls;
//         this.isLoading = false;
//       },
//       error: (error) => {
//         console.error('Ошибка загрузки залов:', error);
//         this.isLoading = false;
//       }
//     });
//   }

//   selectHall(event: Event | string) {
//     let id: string;

//     if (typeof event === 'string') {
//       id = event;
//     } else {
//       const target = event.target as HTMLSelectElement;
//       id = target.value;
//     }

//     if (!id) {
//       this.createNewHall();
//       return;
//     }

//     this.selectedHallId = +id;
//     this.loadHallDetails(+id);
//   }

//   createNewHall() {
//     this.selectedHallId = null;
//     this.hallName = '';
//     this.hallType = '2D';
//     this.basePrice = 300;
//     this.generateLayout(5, 8);
//   }

//   loadHallDetails(hallId: number): void {
//     this.isLoading = true;
//     this.hallService.getHallById(hallId).subscribe({
//       next: (hall) => {
//         this.hallName = hall.hallName || '';
//         this.hallType = hall.hallType || '2D';
//         this.basePrice = hall.basePrice || 300;
        
//         // Если у зала есть информация о местах, используем её
//         if (hall.seatList && hall.seatList.length > 0) {
//           this.transformSeatsToLayout(hall.seatList);
//         } else {
//           // Иначе генерируем базовую схему по размерам зала
//           this.generateLayout(hall.rowsCount || 5, hall.seatsPerRow || 8);
//         }
//         this.isLoading = false;
//       },
//       error: (error) => {
//         console.error('Ошибка загрузки деталей зала:', error);
//         this.isLoading = false;
//       }
//     });
//   }

//   transformSeatsToLayout(seats: SeatDto[]): void {
//     if (!seats || seats.length === 0) {
//       this.generateLayout(5, 8);
//       return;
//     }

//     // Находим максимальные значения рядов и мест
//     let maxRow = 0;
//     let maxSeat = 0;
    
//     seats.forEach(seat => {
//       if (seat.rowNumber > maxRow) maxRow = seat.rowNumber;
//       if (seat.seatNumber > maxSeat) maxSeat = seat.seatNumber;
//     });
    
//     // Создаем двумерный массив
//     this.layout = [];
//     for (let r = 1; r <= maxRow; r++) {
//       const rowSeats = seats.filter(s => s.rowNumber === r);
//       const row: SeatDto[] = [];
      
//       // Создаем места для каждого номера в ряду
//       for (let s = 1; s <= maxSeat; s++) {
//         const seat = rowSeats.find(rs => rs.seatNumber === s);
//         if (seat) {
//           // Если место существует в данных
//           row.push({
//             ...seat,
//             seatId: seat.seatId || 0,
//             hallId: this.selectedHallId || 0
//           });
//         } else {
//           // Создаем стандартное место
//           row.push({
//             seatId: 0,
//             rowNumber: r,
//             seatNumber: s,
//             seatType: 'standard',
//             priceMultiplier: 1,
//             hallId: this.selectedHallId || 0
//           });
//         }
//       }
      
//       this.layout.push(row);
//     }
//   }

//   generateLayout(rows: number, seatsPerRow: number) {
//     this.layout = [];
//     for (let r = 1; r <= rows; r++) {
//       const row: SeatDto[] = [];
//       for (let s = 1; s <= seatsPerRow; s++) {
//         row.push({
//           seatId: 0,
//           rowNumber: r,
//           seatNumber: s,
//           seatType: 'standard',
//           priceMultiplier: 1,
//           hallId: this.selectedHallId || 0
//         });
//       }
//       this.layout.push(row);
//     }
//   }

//   seatClick(seat: SeatDto) {
//     if (!seat) return;

//     switch (this.activeTool) {
//       case 'vip':
//         seat.seatType = seat.seatType === 'vip' ? 'standard' : 'vip';
//         seat.priceMultiplier = seat.seatType === 'vip' ? 1.5 : 1.0;
//         break;
//       case 'block':
//         seat.seatType = seat.seatType === 'blocked' ? 'standard' : 'blocked';
//         break;
//       case 'select':
//       default:
//         break;
//     }
//   }

//   addRow() {
//     const rowNumber = this.layout.length + 1;
//     const seatsPerRow = this.layout[0]?.length || 8;
//     const newRow: SeatDto[] = [];

//     for (let i = 1; i <= seatsPerRow; i++) {
//       newRow.push({
//         seatId: 0,
//         rowNumber,
//         seatNumber: i,
//         seatType: 'standard',
//         priceMultiplier: 1,
//         hallId: this.selectedHallId || 0
//       });
//     }
//     this.layout.push(newRow);
//   }

//   addSeatRight() {
//     this.layout.forEach((row, rIndex) => {
//       const newSeatNumber = row.length + 1;
//       row.push({
//         seatId: 0,
//         rowNumber: rIndex + 1,
//         seatNumber: newSeatNumber,
//         seatType: 'standard',
//         priceMultiplier: 1,
//         hallId: this.selectedHallId || 0
//       });
//     });
//   }

//   removeRow() {
//     if (this.layout.length > 0) {
//       this.layout.pop();
//     }
//   }

//   deleteHall() {
//     if (this.selectedHallId !== null && confirm('Удалить зал?')) {
//       this.isLoading = true;
//       this.hallService.deleteHall(this.selectedHallId).subscribe({
//         next: () => {
//           this.halls = this.halls.filter(h => h.hallId !== this.selectedHallId);
//           this.createNewHall();
//           alert('Зал успешно удален!');
//           this.isLoading = false;
//         },
//         error: (error) => {
//           console.error('Ошибка удаления зала:', error);
//           alert('Ошибка при удалении зала');
//           this.isLoading = false;
//         }
//       });
//     }
//   }

//   saveHall() {
//     if (!this.hallName) {
//       alert("Введите название зала!");
//       return;
//     }
//   // Проверка прав администратора
//   if (!this.authService.isAdmin()) {
//     alert('Только администраторы могут редактировать залы');
//     return;
//   }

//     this.isLoading = true;
    
//     // Собираем все места в плоский массив
//     const allSeats: SeatDto[] = [];
//     this.layout.forEach(row => {
//       allSeats.push(...row);
//     });

//     const hallData: HallDto = {
//       hallId: this.selectedHallId || 0,
//       hallName: this.hallName,
//       status: 'active',
//       basePrice: this.basePrice,
//       rowsCount: this.layout.length,
//       seatsPerRow: this.layout[0]?.length || 8,
//       hallType: this.hallType,
//       seatList: allSeats
//     };

//     if (this.selectedHallId) {
//       // Обновление существующего зала
//       this.hallService.updateHall(this.selectedHallId, hallData).subscribe({
//         next: (updatedHall) => {
//           // Обновляем в списке
//           const index = this.halls.findIndex(h => h.hallId === this.selectedHallId);
//           if (index !== -1) {
//             this.halls[index] = updatedHall;
//           }
//           alert(`Зал "${this.hallName}" обновлён!`);
//           this.isLoading = false;
//         },
//         error: (error) => {
//           console.error('Ошибка обновления зала:', error);
//           alert('Ошибка при обновлении зала');
//           this.isLoading = false;
//         }
//       });
//     } else {
//       // Создание нового зала
//       this.hallService.createHall(hallData).subscribe({
//         next: (createdHall) => {
//           this.halls.push(createdHall);
//           this.selectedHallId = createdHall.hallId;
//           alert(`Зал "${this.hallName}" создан!`);
//           this.isLoading = false;
//         },
//         error: (error) => {
//           console.error('Ошибка создания зала:', error);
//           alert('Ошибка при создании зала');
//           this.isLoading = false;
//         }
//       });
//     }
//   }

//   totalSeats(): number {
//     return this.layout.reduce((sum, row) => sum + row.length, 0);
//   }

//   vipSeats(): number {
//     return this.layout.reduce((sum, row) => sum + row.filter(seat => seat.seatType === 'vip').length, 0);
//   }

//   blockedSeats(): number {
//     return this.layout.reduce((sum, row) => sum + row.filter(seat => seat.seatType === 'blocked').length, 0);
//   }
// }

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HallService } from '../../core/services/hall.service';
import { HallDto, SeatDto } from '../../shared/models';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-admin-hall-constructor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-hall-constructor.html',
  styleUrls: ['./admin-hall-constructor.css']
})
export class AdminHallConstructor implements OnInit {

  halls: HallDto[] = [];
  selectedHallId: number | null = null;
  hallName = '';
  hallType = '2D';
  basePrice = 300;
  activeTool: 'select' | 'vip' | 'block' = 'select';
  isLoading = false;
  
  // Поля для размеров зала
  rowsCount = 5;
  seatsPerRowCount = 8;

  // layout: двумерный массив мест
  layout: SeatDto[][] = [];

  constructor(private hallService: HallService, private route: ActivatedRoute) {}

  ngOnInit() {
    this.loadHalls();
    this.route.queryParams.subscribe(params => {
      const editHallId = params['editHallId'];
      if (editHallId) {
        setTimeout(() => {
          this.selectHall(editHallId);
        }, 100);
      }
    });
  }

  loadHalls(): void {
    this.isLoading = true;
    this.hallService.getAllHalls().subscribe({
      next: (halls) => {
        this.halls = halls;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка загрузки залов:', error);
        this.isLoading = false;
      }
    });
  }

  selectHall(event: Event | string) {
    let id: string;

    if (typeof event === 'string') {
      id = event;
    } else {
      const target = event.target as HTMLSelectElement;
      id = target.value;
    }

    if (!id) {
      this.createNewHall();
      return;
    }

    this.selectedHallId = +id;
    this.loadHallDetails(+id);

        setTimeout(() => {
      const selectElement = document.querySelector('select') as HTMLSelectElement;
      if (selectElement) {
        selectElement.value = id;
      }
    }, 0);
  }

  createNewHall() {
    this.selectedHallId = null;
    this.hallName = '';
    this.hallType = '2D';
    this.basePrice = 300;
    this.rowsCount = 5;
    this.seatsPerRowCount = 8;
    this.generateLayout(this.rowsCount, this.seatsPerRowCount);

        setTimeout(() => {
      const selectElement = document.querySelector('select') as HTMLSelectElement;
      if (selectElement) {
        selectElement.value = '';
      }
    }, 0);
  }

  loadHallDetails(hallId: number): void {
    this.isLoading = true;
    this.hallService.getHallById(hallId).subscribe({
      next: (hall) => {
        this.hallName = hall.hallName || '';
        this.hallType = hall.hallType || '2D';
        this.basePrice = hall.basePrice || 300;
        this.rowsCount = hall.rowsCount || 5;
        this.seatsPerRowCount = hall.seatsPerRow || 8;
        
        // Если у зала есть информация о местах, используем её
        if (hall.seatList && hall.seatList.length > 0) {
          this.transformSeatsToLayout(hall.seatList);
        } else {
          // Иначе генерируем базовую схему по размерам зала
          this.generateLayout(this.rowsCount, this.seatsPerRowCount);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка загрузки деталей зала:', error);
        this.isLoading = false;
      }
    });
  }

transformSeatsToLayout(seats: SeatDto[]): void {
  if (!seats || seats.length === 0) {
    this.generateLayout(this.rowsCount, this.seatsPerRowCount);
    return;
  }

  // Находим максимальные значения рядов и мест
  let maxRow = 0;
  let maxSeat = 0;
  
  seats.forEach(seat => {
    if (seat.rowNumber > maxRow) maxRow = seat.rowNumber;
    if (seat.seatNumber > maxSeat) maxSeat = seat.seatNumber;
  });
  
  // Обновляем счетчики
  this.rowsCount = maxRow;
  this.seatsPerRowCount = maxSeat;
  
  // Создаем двумерный массив
  this.layout = [];
  for (let r = 1; r <= maxRow; r++) {
    const rowSeats = seats.filter(s => s.rowNumber === r);
    const row: SeatDto[] = [];
    
    // Создаем места для каждого номера в ряду
    for (let s = 1; s <= maxSeat; s++) {
      const seat = rowSeats.find(rs => rs.seatNumber === s);
      if (seat) {
        // Если место существует в данных - сохраняем ВСЕ оригинальные поля
        row.push({
          seatId: seat.seatId, // ← Сохраняем оригинальный seatId!
          rowNumber: seat.rowNumber,
          seatNumber: seat.seatNumber,
          seatType: seat.seatType,
          priceMultiplier: seat.priceMultiplier || (seat.seatType === 'vip' ? 1.5 : 1.0),
          hallId: seat.hallId || this.selectedHallId || 0,
          status: seat.status
        });
      } else {
        // Создаем новое место (seatId = 0)
        row.push({
          seatId: 0,
          rowNumber: r,
          seatNumber: s,
          seatType: 'standard',
          priceMultiplier: 1.0,
          hallId: this.selectedHallId || 0,
          status: 'AVALIBLE'
        });
      }
    }
    
    this.layout.push(row);
  }
}

  generateLayout(rows: number, seatsPerRow: number) {
    this.layout = [];
    for (let r = 1; r <= rows; r++) {
      const row: SeatDto[] = [];
      for (let s = 1; s <= seatsPerRow; s++) {
        row.push({
          seatId: 0,
          rowNumber: r,
          seatNumber: s,
          seatType: 'standard',
          priceMultiplier: 1.0,
          hallId: this.selectedHallId || 0,
          status: 'AVALIBLE'
        });
      }
      this.layout.push(row);
    }
  }

  // Метод для обновления размеров зала
  updateLayoutDimensions() {
    const currentRows = this.layout.length;
    const currentSeatsPerRow = this.layout[0]?.length || 0;
    
    // Если размеры не изменились, ничего не делаем
    if (currentRows === this.rowsCount && currentSeatsPerRow === this.seatsPerRowCount) {
      return;
    }
    
    // Сохраняем текущие места для копирования данных
    const oldLayout = [...this.layout];
    
    // Создаем новую схему
    this.generateLayout(this.rowsCount, this.seatsPerRowCount);
    
    // Копируем типы мест из старой схемы
    for (let r = 0; r < Math.min(oldLayout.length, this.layout.length); r++) {
      for (let s = 0; s < Math.min(oldLayout[r].length, this.layout[r].length); s++) {
        this.layout[r][s].seatType = oldLayout[r][s].seatType;
        this.layout[r][s].priceMultiplier = oldLayout[r][s].priceMultiplier;
      }
    }
  }

  // Метод для получения CSS класса места
  getSeatClass(seat: SeatDto): string {
    if (!seat) return 'seat';
    
    let classes = 'seat';
    
    switch(seat.seatType) {
      case 'vip':
        classes += ' vip';
        break;
      case 'blocked':
        classes += ' blocked';
        break;
      default:
        classes += ' standard';
    }
    
    return classes;
  }

  seatClick(seat: SeatDto) {
    if (!seat) return;

    switch (this.activeTool) {
      case 'vip':
        seat.seatType = seat.seatType === 'vip' ? 'standard' : 'vip';
        seat.priceMultiplier = seat.seatType === 'vip' ? 1.5 : 1.0;
        break;
      case 'block':
        seat.seatType = seat.seatType === 'blocked' ? 'standard' : 'blocked';
        seat.priceMultiplier = 1.0;
        break;
      case 'select':
      default:
        break;
    }
  }

  deleteHall() {
    if (this.selectedHallId !== null && confirm('Удалить зал?')) {
      this.isLoading = true;
      this.hallService.deleteHall(this.selectedHallId).subscribe({
        next: () => {
          this.halls = this.halls.filter(h => h.hallId !== this.selectedHallId);
          this.createNewHall();
          alert('Зал успешно удален!');
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Ошибка удаления зала:', error);
          alert('Ошибка при удалении зала');
          this.isLoading = false;
        }
      });
    }
  }

  saveHall() {
    if (!this.hallName) {
      alert("Введите название зала!");
      return;
    }

    // Обновляем схему перед сохранением
    this.updateLayoutDimensions();

    this.isLoading = true;
    
    // Собираем все места в плоский массив
    const allSeats: SeatDto[] = [];
    this.layout.forEach(row => {
      allSeats.push(...row);
    });

    const hallData: any = {
      hallId: this.selectedHallId || 0,
      hallName: this.hallName,
      status: 'active',
      basePrice: this.basePrice,
      rowsCount: this.layout.length,
      seatsPerRow: this.layout[0]?.length || 8,
      hallType: this.hallType,
      seatList: allSeats
    };

    console.log('Отправляемые данные:', hallData);

    if (this.selectedHallId) {
      // Обновление существующего зала
      this.hallService.updateHall(this.selectedHallId, hallData).subscribe({
        next: (updatedHall) => {
          // Обновляем в списке
          const index = this.halls.findIndex(h => h.hallId === this.selectedHallId);
          if (index !== -1) {
            this.halls[index] = updatedHall;
          }
          alert(`Зал "${this.hallName}" обновлён!`);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Ошибка обновления зала:', error);
          console.error('Детали ошибки:', error.error);
          alert(`Ошибка при обновлении зала: ${error.error?.message || error.message}`);
          this.isLoading = false;
        }
      });
    } else {
      // Создание нового зала
      this.hallService.createHall(hallData).subscribe({
        next: (createdHall) => {
          this.halls.push(createdHall);
          this.selectedHallId = createdHall.hallId;
          alert(`Зал "${this.hallName}" создан!`);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Ошибка создания зала:', error);
          console.error('Детали ошибки:', error.error);
          alert(`Ошибка при создании зала: ${error.error?.message || error.message}`);
          this.isLoading = false;
        }
      });
    }
  }

  totalSeats(): number {
    return this.layout.reduce((sum, row) => sum + row.length, 0);
  }

  vipSeats(): number {
    return this.layout.reduce((sum, row) => sum + row.filter(seat => seat.seatType === 'vip').length, 0);
  }

  blockedSeats(): number {
    return this.layout.reduce((sum, row) => sum + row.filter(seat => seat.seatType === 'blocked').length, 0);
  }
}