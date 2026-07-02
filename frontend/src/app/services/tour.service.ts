import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Tour } from '../models/tour.model';

// this is the go between for tours: the screens ask it for tours and it talks to the backend.
// it keeps its own live list of tours in a signal (a box that screens can watch, so when the
// list changes the screens redraw by themselves). every screen reads from this one shared list.
@Injectable({ providedIn: 'root' })
export class TourService {

  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/tours';

  // the private list we change, and the read only view of it that screens are allowed to watch
  private _tours = signal<Tour[]>([]);
  readonly tours = this._tours.asReadonly();

  // fetch every tour from the backend and put them in the shared list
  loadAll(): void {
    this.http.get<Tour[]>(this.apiUrl).subscribe((tours: Tour[]) => {
      this._tours.set(tours);
    });
  }

  // ask the backend for tours matching a typed word and show those instead
  search(query: string): void {
    this.http.get<Tour[]>(this.apiUrl + '/search?query=' + query).subscribe((tours: Tour[]) => {
      this._tours.set(tours);
    });
  }

  // send a new tour to the backend, and once saved add it to the shared list
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

  // save changes to a tour, then swap the old copy in the shared list for the updated one
  update(tour: Tour): void {
    this.http.put<Tour>(`${this.apiUrl}/${tour.id}`, tour).subscribe((updated: Tour) => {
      this._tours.update(current =>
        current.map(t => t.id === updated.id ? updated : t)
      );
    });
  }

  // ask the backend to delete a tour, then drop it from the shared list
  delete(id: number): void {
    this.http.delete<void>(`${this.apiUrl}/${id}`).subscribe(() => {
      this._tours.update(current => current.filter(t => t.id !== id));
    });
  }

  // download all tours as a file.
  // step by step: get the tours, turn them into text, wrap that text in a file the browser
  // can hold, then make a hidden link and click it for the user so the file saves.
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

  // load tours back in from a file the user picked.
  // step by step: read the file's text, check it really is a list of tours, send it to the
  // backend, and once it is in, reload the list so the new tours show up.
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