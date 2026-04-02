// IF CHANGE MESSAGE FIRST!

import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';

import { TourList } from './tour-list/tour-list.component';
import { TourDetails } from './tour-details/tour-details.component';
import { TourLogList } from './tour-logs/tour-log.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, TourList, TourDetails, TourLogList],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'tour-planner';
}