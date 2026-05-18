import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TourLog } from '../models/tour-log.model';

@Injectable({ providedIn: 'root' })
export class TourLogService {

  private http = inject(HttpClient);
  private apiBase = 'http://localhost:8080/api/tours';

  private _logs = signal<TourLog[]>([]);
  readonly logs = this._logs.asReadonly();

  loadForTour(tourId: number): void {
    this.http.get<TourLog[]>(`${this.apiBase}/${tourId}/logs`).subscribe(logs => {
      this._logs.set(logs);
    });
  }

  add(log: TourLog): void {
    this.http.post<TourLog>(`${this.apiBase}/${log.tourId}/logs`, log).subscribe(created => {
      this._logs.update(current => [...current, created]);
    });
  }

  update(log: TourLog): void {
    this.http.put<TourLog>(`${this.apiBase}/${log.tourId}/logs/${log.id}`, log).subscribe(updated => {
      this._logs.update(current =>
        current.map(l => l.id === updated.id ? updated : l)
      );
    });
  }

  delete(id: number, tourId: number): void {
    this.http.delete(`${this.apiBase}/${tourId}/logs/${id}`).subscribe(() => {
      this._logs.update(current => current.filter(l => l.id !== id));
    });
  }
}