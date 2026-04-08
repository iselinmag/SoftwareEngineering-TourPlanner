import { Injectable, signal, computed } from '@angular/core';
import { Tour, TransportType } from '../models/tour.model';

@Injectable({
  providedIn: 'root',
})
export class TourListViewmodel {
  // 1. State where we keep all our dummy tours.
  tours = signal<Tour[]>([
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
  ]);

  // 2. State that tracks which tour is currently selected
  selectedTourId = signal<string | null>(null);

  // 3. A computed signal that automatically finds the full tour object based on the selected ID
  selectedTour = computed(() => {
    const id = this.selectedTourId();
    if (!id) return null;
    return this.tours().find(t => t.id === id) || null;
  });

  // 4. Method to update the selected tour
  selectTour(id: string) {
    this.selectedTourId.set(id);
  }

  // Create tour
  addTour(tour: Tour) {
    // 1. Generate a temp ID
    tour.id = Date.now().toString(); 
    
    // 2. Update the signal by copying the old array and adding the new tour element at the end
    this.tours.update(tours => [...tours, tour]);
  }

  // Update tour
  updateTour(updatedTour: Tour) {
    this.tours.update(tours => 
      tours.map(tour => tour.id === updatedTour.id ? updatedTour : tour)
    );
  }

  // Delete tour
  deleteTour(id: string) {
    // 1. Filter out the deleted tour
    this.tours.update(tours => tours.filter(tour => tour.id !== id));
    
    // 2. If we delete the currently selected tour then we reset the view
    if (this.selectedTourId() === id) {
      this.selectedTourId.set(null);
    }
  }
}
