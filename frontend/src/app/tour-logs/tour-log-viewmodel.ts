import { Injectable, signal, computed, inject } from '@angular/core';
import { TourLog, Difficulty } from '../models/tour-log.model';
import { TourLogService } from '../services/tour-log.service';

// TourLogViewmodel is now ONLY responsible for UI state:
//   - Which tour is currently selected (so we know which logs to show)
//   - Filtering the logs list to only the selected tour
//   - Forwarding add/update/delete calls to TourLogService
//   - Form validation (belongs to the UI layer, not the data layer)
//
// The actual data and CRUD logic live in TourLogService.
@Injectable({
  providedIn: 'root', // makes this service a shared singleton across the whole app
})
export class TourLogViewmodel {

  // Holds the ID of the currently selected tour
  // null means no tour is selected
  selectedTourId = signal<number | null>(null);

   // Inject the data service
  private logService = inject(TourLogService);

  // Now just expose all logs loaded for the current tour
  // (the service already filters them via loadForTour)
  readonly filteredLogs = this.logService.logs;

  // Updates which tour is currently selected
  // Called from the tour list component
  setSelectedTour(tourId: number) {         // was string
    this.selectedTourId.set(tourId);
    this.logService.loadForTour(tourId);    // fetch from backend
  }

  // --- Delegate CRUD to the service ---

  addLog(log: TourLog) {
    this.logService.add(log);
  }

  updateLog(updatedLog: TourLog) {
    this.logService.update(updatedLog);
  }

  deleteLog(id: number, tourId: number) {   // add tourId parameter
    this.logService.delete(id, tourId);
  }
  // Validation belongs in the ViewModel (UI concern, not data concern)
  isValid(log: TourLog): boolean {
    return (
      !!log.comment &&
      log.rating >= 1 &&
      log.rating <= 5 &&
      !!log.dateTime
    );
  }
}