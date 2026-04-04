
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
    description: 'A walk through the historic city center.',
    fromLocation: 'Stephansplatz',
    toLocation: 'Prater',
    transportType: TransportType.Hike,
    distance: 5,
    estimatedTime: '01:30'
  },
  {
    id: 'tour2',
    name: 'Danube Riverside Ride',
    description: 'A scenic bike ride along the Danube canal.',
    fromLocation: 'Schwedenplatz',
    toLocation: 'Donauinsel',
    transportType: TransportType.Bike,
    distance: 14,
    estimatedTime: '01:10'
  },
  {
    id: 'tour3',
    name: 'Belvedere to Schönbrunn Run',
    description: 'A longer running route connecting two major landmarks.',
    fromLocation: 'Belvedere Palace',
    toLocation: 'Schönbrunn Palace',
    transportType: TransportType.Running,
    distance: 11,
    estimatedTime: '01:05'
  },
  {
    id: 'tour4',
    name: 'Vienna Woods Escape',
    description: 'A relaxing outdoor hike in the Vienna Woods.',
    fromLocation: 'Neuwaldegg',
    toLocation: 'Kahlenberg',
    transportType: TransportType.Hike,
    distance: 9,
    estimatedTime: '02:15'
  },
  {
    id: 'tour5',
    name: 'Weekend Lake Getaway',
    description: 'A vacation-style trip to a nearby lake area.',
    fromLocation: 'Vienna',
    toLocation: 'Neusiedler See',
    transportType: TransportType.Vacation,
    distance: 60,
    estimatedTime: '04:00'
  },
  {
    id: 'tour6',
    name: 'Museum Quarter Loop',
    description: 'A short urban walk between cultural highlights.',
    fromLocation: 'MuseumsQuartier',
    toLocation: 'Karlsplatz',
    transportType: TransportType.Hike,
    distance: 3,
    estimatedTime: '00:45'
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