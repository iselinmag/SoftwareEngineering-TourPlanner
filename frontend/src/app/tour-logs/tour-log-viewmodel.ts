import { Injectable, signal, computed } from '@angular/core';
import { TourLog, Difficulty } from '../models/tour-log.model';

@Injectable({
  providedIn: 'root',
})
export class TourLogViewmodel {

  // 🔹 Hvilken tour som er valgt (må settes fra tour-list)
  selectedTourId = signal<string | null>(null);

  // 🔹 Alle logs
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

  // 🔹 Filterte logs basert på valgt tour (Task 1)
  filteredLogs = computed(() => {
    if (!this.selectedTourId()) return [];
    return this.logs().filter(
      log => log.tourId === this.selectedTourId()
    );
  });

  // 🔹 Set valgt tour (kalles fra tour-list)
  setSelectedTour(tourId: string) {
    this.selectedTourId.set(tourId);
  }

  // 🔹 CREATE (Task 2)
  addLog(log: TourLog) {
    this.logs.update(logs => [...logs, log]);
  }

  // 🔹 DELETE (Task 2)
  deleteLog(id: string) {
    this.logs.update(logs => logs.filter(log => log.id !== id));
  }

  // 🔹 UPDATE (Task 2)
  updateLog(updatedLog: TourLog) {
    this.logs.update(logs =>
      logs.map(log =>
        log.id === updatedLog.id ? updatedLog : log
      )
    );
  }

  // 🔹 VALIDATION (Task 3 - enkel versjon)
  isValid(log: TourLog): boolean {
    return (
      !!log.comment &&
      log.rating >= 1 &&
      log.rating <= 5 &&
      !!log.dateTime
    );
  }
}