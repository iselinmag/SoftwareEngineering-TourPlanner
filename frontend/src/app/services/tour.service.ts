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
    this.http.get<Tour[]>(this.apiUrl).subscribe((tours: Tour[]) => {
      this._tours.set(tours);
    });
  }

  search(query: string): void {
    this.http.get<Tour[]>(this.apiUrl + '/search?query=' + query).subscribe((tours: Tour[]) => {
      this._tours.set(tours);
    });
  }

  add(tour: Tour, onSuccess?: () => void): void {
    this.http.post<Tour>(this.apiUrl, tour).subscribe({
      next: (created: Tour) => {
        this._tours.update(current => [...current, created]);
        if (onSuccess) onSuccess();
      },
      error: (err: unknown) => {
        console.error('Failed to create tour:', err);
        alert('Could not create tour. Check the backend logs for details.');
      }
    });
  }

  update(tour: Tour): void {
    this.http.put<Tour>(`${this.apiUrl}/${tour.id}`, tour).subscribe((updated: Tour) => {
      this._tours.update(current =>
        current.map(t => t.id === updated.id ? updated : t)
      );
    });
  }

  delete(id: number): void {
    this.http.delete<void>(`${this.apiUrl}/${id}`).subscribe(() => {
      this._tours.update(current => current.filter(t => t.id !== id));
    });
  }

  exportTours(): void {
    this.http.get<Tour[]>(`${this.apiUrl}/export`).subscribe({
      next: (tours: Tour[]) => {
        const json = JSON.stringify(tours, null, 2);
        const blob = new Blob([json], { type: 'application/json' });

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');

        link.href = url;
        link.download = 'tours.json';
        link.click();

        window.URL.revokeObjectURL(url);
      },
      error: (err: unknown) => {
        console.error('Failed to export tours:', err);
        alert('Could not export tours.');
      }
    });
  }

  importTours(file: File): void {
    const reader = new FileReader();

    reader.onload = () => {
      try {
        const content = reader.result as string;
        const tours = JSON.parse(content) as Tour[];

        if (!Array.isArray(tours)) {
          alert('Invalid import file. Expected a JSON array of tours.');
          return;
        }

        this.http.post<void>(`${this.apiUrl}/import`, tours).subscribe({
          next: () => {
            this.loadAll();
            alert('Tours imported successfully.');
          },
          error: (err: unknown) => {
            console.error('Failed to import tours:', err);
            alert('Could not import tours.');
          }
        });
      } catch (error) {
        console.error('Invalid JSON file:', error);
        alert('Invalid JSON file.');
      }
    };

    reader.readAsText(file);
  }
}