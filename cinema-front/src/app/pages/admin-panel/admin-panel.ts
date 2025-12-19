import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-panel',
  templateUrl: './admin-panel.html',
  styleUrls: ['./admin-panel.css'],
  standalone: true,
  imports: [RouterModule]
})
export class AdminPanel {}
