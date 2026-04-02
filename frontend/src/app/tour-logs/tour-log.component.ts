import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourLogViewmodel } from './tour-log-viewmodel';

@Component({
  selector: 'app-tour-log',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tour-log.component.html',
  styleUrl: './tour-log.component.css',
})
export class TourLogList {
  vm = inject(TourLogViewmodel);
}