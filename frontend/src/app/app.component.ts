// IF CHANGE MESSAGE FIRST!

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';


import { TourList } from './tour-list/tour-list.component';
import { TourDetails } from './tour-details/tour-details.component';
import { TourLogList } from './tour-logs/tour-log.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, TourList, TourDetails, TourLogList],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'tour-planner';
}