import { Injectable, signal, computed, inject } from '@angular/core';
import { TourLog, Difficulty } from '../models/tour-log.model';
import { TourLogService } from '../services/tour-log.service';

// this is the middle layer for the logs screen (a viewmodel).
// like a waiter, the screen talks only to this, and this passes the real work to the log
// service kitchen. it also remembers which tour is open and checks the form before saving.
@Injectable({
  providedIn: 'root', // one shared copy used everywhere in the app
})
export class TourLogViewmodel {

  // the id of the tour currently open, or nothing if none is picked
  selectedTourId = signal<number | null>(null);

  // the kitchen we send all the real work to
  private logService = inject(TourLogService);

  // the logs for the open tour, passed straight through from the service so the screen shows them
  readonly filteredLogs = this.logService.logs;

  // remember which tour is now open and ask the service to load its logs.
  // called from the tour list when a tour card is clicked.
  setSelectedTour(tourId: number) {
    this.selectedTourId.set(tourId);
    this.logService.loadForTour(tourId);
  }

  // the calls below just pass the order straight to the service kitchen

  // add a new log
  addLog(log: TourLog) {
    this.logService.add(log);
  }

  // save changes to a log
  updateLog(updatedLog: TourLog) {
    this.logService.update(updatedLog);
  }

  // delete a log, passing both the log and its tour since the service needs both
  deleteLog(id: number, tourId: number) {
    this.logService.delete(id, tourId);
  }

  // check a log has the basics before saving: a comment, a rating from 1 to 5, and a date
  isValid(log: TourLog): boolean {
    return (
      !!log.comment &&
      log.rating >= 1 &&
      log.rating <= 5 &&
      !!log.dateTime
    );
  }
}