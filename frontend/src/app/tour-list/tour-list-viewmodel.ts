import { Injectable, signal, computed, inject, OnInit} from '@angular/core';
import { Tour, TransportType } from '../models/tour.model';
import { TourService } from '../services/tour.service';

// this is the middle layer for the tour list screen (a viewmodel).
// think of it like a waiter: the screen talks only to this, and this passes food orders to
// the kitchen (the tour service) and remembers small screen only things like which tour is
// picked and what was typed in search. the real data and saving live in the service, not here.
@Injectable({
  providedIn: 'root',
})
export class TourListViewmodel {
  // the kitchen we send all the real work to
  private tourService = inject(TourService);

  // the live tour list, passed straight through from the service so the screen can show it
  readonly tours = this.tourService.tours;

  constructor() {
    // when the screen first appears, ask the service to fetch all tours
    this.tourService.loadAll();
  }

  // the id of the tour the user has clicked, or nothing if none is picked yet.
  // it is a signal (a watchable box) so every screen updates on its own when it changes.
  selectedTourId = signal<number | null>(null);

  // the full tour that matches the picked id, worked out from the list, or nothing if none
  selectedTour = computed(() => {
    const id = this.selectedTourId();
    if (!id) return null;
    return this.tours().find(t => t.id === id) || null;
  });

  // remember which tour was just clicked
  selectTour(id: number) {
    this.selectedTourId.set(id);
  }

  // the word currently typed in the search box, kept in a signal so other screens can read it
  currentSearchTerm = signal<string>("");

  // run a search: remember the word, and if it is empty just show all tours again
  search(query: string) {
    this.currentSearchTerm.set(query);
    if (!query) {
      return this.tourService.loadAll();
    }
    return this.tourService.search(query)
  }

  // the calls below just pass the order straight to the service kitchen

  // delete a tour, and if it was the one on screen, clear the selection so nothing dangles
  deleteTour(id: number) {
    this.tourService.delete(id);
    if (this.selectedTourId() === id) {
      this.selectedTourId.set(null);
    }
  }

  // add a new tour
  addTour(tour: Tour, onSuccess?: () => void) {
    this.tourService.add(tour, onSuccess);
  }

  // save changes to a tour
  updateTour(updatedTour: Tour) {
    this.tourService.update(updatedTour);
  }

  // save every tour to a file
  exportTours(): void {
    this.tourService.exportTours();
  }

  // load tours back in from a file
  importTours(file: File): void {
    this.tourService.importTours(file);
  }
}
