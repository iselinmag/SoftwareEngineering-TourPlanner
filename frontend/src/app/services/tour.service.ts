import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Tour } from '../models/tour.model';

@Injectable({ providedIn: 'root' })
export class TourService {

  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/tours';

  private _tours = signal<Tour[]>([]);
  readonly tours = this._tours.asReadonly(); // what is this?

  loadAll(): void {
    this.http.get<Tour[]>(this.apiUrl).subscribe(tours => {
      this._tours.set(tours);
    });
  }

  search(query: string): void {
    this.http.get<Tour[]>(this.apiUrl + "/search?query=" + query).subscribe(tours => {
      this._tours.set(tours);
    })
  }

  add(tour: Tour, onSuccess?: () => void): void {
    this.http.post<Tour>(this.apiUrl, tour).subscribe({
      next: (created) => {
        this._tours.update(current => [...current, created]);
        if (onSuccess) onSuccess();
      },
      error: (err) => {
        console.error('Failed to create tour:', err);
        alert('Could not create tour. Check the backend logs for details.');
      }
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