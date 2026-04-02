
// <Matej

//!!!!!example code from chatgpt so youre connected to my tour-log viewmodel with THECORRECT ID

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourLogViewmodel } from '../tour-logs/tour-log-viewmodel';
import { Tour, TransportType } from '../models/tour.model';

@Component({
  selector: 'app-tour-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tour-list.component.html',
  styleUrl: './tour-list.component.css',
})
export class TourList {
  tours: Tour[] = [
    {
      id: 'tour1',
      name: 'Vienna City Walk',
      description: 'A walk through the city center',
      fromLocation: 'Stephansplatz',
      toLocation: 'Prater',
      transportType: TransportType.Hike,
      distance: 5,
      estimatedTime: '01:30'
    },
    {
      id: 'tour2',
      name: 'Mountain Route',
      description: 'A harder route in the hills',
      fromLocation: 'Start Point',
      toLocation: 'Viewpoint',
      transportType: TransportType.Bike,
      distance: 20,
      estimatedTime: '03:00'
    }
  ];

  selectedTourId: string | null = null;

  constructor(public logVm: TourLogViewmodel) {}

  selectTour(tour: Tour) {
    this.selectedTourId = tour.id ?? null;

    if (tour.id) {
      this.logVm.setSelectedTour(tour.id);
    }
  }
}