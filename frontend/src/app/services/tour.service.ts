import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Tour } from '../models/tour.model';

@Injectable({ providedIn: 'root' })
export class TourService {

  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/tours';

  private _tours = signal<Tour[]>([]);
  readonly tours = this._tours.asReadonly();

  loadAll(): void {
    this.http.get<Tour[]>(this.apiUrl).subscribe(tours => {
      this._tours.set(tours);
    });
  }

  add(tour: Tour): void {
    this.http.post<Tour>(this.apiUrl, tour).subscribe(created => {
      this._tours.update(current => [...current, created]);
    });
  }

  update(tour: Tour): void {
    this.http.put<Tour>(`${this.apiUrl}/${tour.id}`, tour).subscribe(updated => {
      this._tours.update(current =>
        current.map(t => t.id === updated.id ? updated : t)
      );
    });
  }

  delete(id: number): void {
    this.http.delete(`${this.apiUrl}/${id}`).subscribe(() => {
      this._tours.update(current => current.filter(t => t.id !== id));
    });
  }
}