import { Injectable, signal, computed } from '@angular/core';
import { Tour, TransportType } from '../models/tour.model';

// @Injectable makes this class a Service.
// providedIn: 'root' means Angular creates ONE shared version of the file that is available for the whole app,
// so every component that injects this gets the same data.

@Injectable({
  providedIn: 'root',
})
export class TourListViewmodel {
  // All tours are stored as a Signal so Angular automatically updates
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

  // Holds the ID of whichever tour the user has clicked.
  // null means nothing is selected yet.
  // we use signal so all components automatically update this value
  selectedTourId = signal<string | null>(null);

  // We use a computed signal that automatically finds the full tour object based on the selected ID
  // It also recalculates instantly whenever selectedTourId or tours change
  selectedTour = computed(() => { 
    const id = this.selectedTourId();
    if (!id) return null;
    // Automatically give the FULL tour object for whichever ID is currently selected
    return this.tours().find(t => t.id === id) || null;
  });

  // This gets called when the user clicks a tour card and it updates the selected ID.
  selectTour(id: string) {
    this.selectedTourId.set(id);
  }

  // Create tour and add it to the list
  addTour(tour: Tour) {
    // Generate a temp ID with timestamp -> TODO: later REPLACE with DATASET ID
    tour.id = Date.now().toString(); 
    
    // Update the signal by copying the old array and adding the new tour element at the end
    this.tours.update(tours => [...tours, tour]);
  }

  // Update an old tour, replaces the old version with the new version in the list
  updateTour(updatedTour: Tour) {
    this.tours.update(tours => 
      tours.map(tour => tour.id === updatedTour.id ? updatedTour : tour)
    );
  }

  // Delete tour
  deleteTour(id: string) {
    // Filter out the deleted tour from the list
    this.tours.update(tours => tours.filter(tour => tour.id !== id));
    
    // If we delete the currently selected tour then we reset the view
    if (this.selectedTourId() === id) {
      this.selectedTourId.set(null);
    }
  }
}
