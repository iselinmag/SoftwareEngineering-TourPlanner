import { Injectable, signal, computed, inject, OnInit} from '@angular/core';
import { Tour, TransportType } from '../models/tour.model';
import { TourService } from '../services/tour.service';

// @Injectable makes this class a Service.
// providedIn: 'root' means Angular creates ONE shared version of the file that is available for the whole app,
// so every component that injects this gets the same data.

// TourListViewmodel is now ONLY responsible for UI state:
//   - Which tour is currently selected
//   - Exposing the tours list (read from TourService)
//   - Forwarding add/update/delete calls to TourService
//
// The actual data and CRUD logic live in TourService.
// This is the separation the professor's feedback asked for.
@Injectable({
  providedIn: 'root',
})
export class TourListViewmodel {
  // All tours are stored as a Signal so Angular automatically updates
    // Inject the data service
  private tourService = inject(TourService);

  // Expose the tours signal from the service so templates can use it
  // exactly as before: listvm.tours()
  readonly tours = this.tourService.tours;

  constructor() {
    // Load tours from backend when the app starts
    this.tourService.loadAll();
  }


  // Holds the ID of whichever tour the user has clicked.
  // null means nothing is selected yet.
  // we use signal so all components automatically update this value
  selectedTourId = signal<number | null>(null);

  selectedTour = computed(() => {
    const id = this.selectedTourId();
    if (!id) return null;
    return this.tours().find(t => t.id === id) || null;
  });

  selectTour(id: number) {        // was string
    this.selectedTourId.set(id);
  }

  deleteTour(id: number) {        // was string
    this.tourService.delete(id);
    if (this.selectedTourId() === id) {
      this.selectedTourId.set(null);
    }
  }

  // --- Delegate CRUD to the service ---

  addTour(tour: Tour) {
    this.tourService.add(tour);
  }

  updateTour(updatedTour: Tour) {
    this.tourService.update(updatedTour);
  }
}
