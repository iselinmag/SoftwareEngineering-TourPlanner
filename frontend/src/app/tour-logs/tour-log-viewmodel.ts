import { Injectable, signal, computed } from '@angular/core';
import { TourLog, Difficulty } from '../models/tour-log.model';



@Injectable({
  providedIn: 'root', // makes this service a shared singleton across the whole app
})
export class TourLogViewmodel {

  // Holds the ID of the currently selected tour
  // null means no tour is selected
  selectedTourId = signal<string | null>(null);

  // Stores all logs in the system
  // Using a signal means the UI will automatically update when this changes
  logs = signal<TourLog[]>([
    {
      id: '1',
      tourId: 'tour1',
      dateTime: '2026-04-02',
      comment: 'Nice route with great scenery.',
      difficulty: Difficulty.Easy,
      totalDistance: 10,
      totalTime: '01:30',
      rating: 4
    },
    {
      id: '2',
      tourId: 'tour2',
      dateTime: '2026-04-01',
      comment: 'Hard but worth it.',
      difficulty: Difficulty.Hard,
      totalDistance: 20,
      totalTime: '03:00',
      rating: 5
    },
    {
      id: '3',
      tourId: 'tour3',
      dateTime: '2026-04-03',
      comment: 'Nice running route with good pace.',
      difficulty: Difficulty.Medium,
      totalDistance: 11,
      totalTime: '01:04',
      rating: 4
    },
    {
      id: '4',
      tourId: 'tour4',
      dateTime: '2026-04-02',
      comment: 'Beautiful views, but quite steep.',
      difficulty: Difficulty.Hard,
      totalDistance: 9,
      totalTime: '02:20',
      rating: 5
    },
    {
      id: '5',
      tourId: 'tour2',
      dateTime: '2026-04-04',
      comment: 'Smooth bike path and very enjoyable.',
      difficulty: Difficulty.Easy,
      totalDistance: 14,
      totalTime: '01:08',
      rating: 5
    }
  ]);

  // Computed value that automatically filters logs based on selected tour
  // Runs every time selectedTourId or logs change
  filteredLogs = computed(() => {
    if (!this.selectedTourId()) return []; // no tour selected → no logs shown

    // Only return logs that belong to the selected tour
    return this.logs().filter(
      log => log.tourId === this.selectedTourId()
    );
  });

  // Updates which tour is currently selected
  // Called from the tour list component
  setSelectedTour(tourId: string) {
    this.selectedTourId.set(tourId);
  }

  // Adds a new log to the list
  // Uses immutable update (creates new array instead of modifying old one)
  addLog(log: TourLog) {
    this.logs.update(logs => [...logs, log]);
  }

  // Removes a log by ID
  deleteLog(id: string) {
    this.logs.update(logs => logs.filter(log => log.id !== id));
  }

  // Updates an existing log
  // Replaces the old log with the updated one
  updateLog(updatedLog: TourLog) {
    this.logs.update(logs =>
      logs.map(log =>
        log.id === updatedLog.id ? updatedLog : log
      )
    );
  }

  // Basic validation check for a log
  // Ensures required fields are filled and rating is valid
  isValid(log: TourLog): boolean {
    return (
      !!log.comment &&           // must have comment
      log.rating >= 1 &&         // rating must be between 1–5
      log.rating <= 5 &&
      !!log.dateTime            // must have date
    );
  }
}