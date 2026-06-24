import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TourLog } from '../models/tour-log.model';

@Injectable({ providedIn: 'root' })
export class TourLogService {

  private http = inject(HttpClient);
  private apiBase = 'http://localhost:8080/api/tours';

  private _logs = signal<TourLog[]>([]);
  readonly logs = this._logs.asReadonly();

  // holds the list of full image urls for the currently selected tour's gallery
  private _images = signal<string[]>([]);
  readonly images = this._images.asReadonly();

  loadForTour(tourId: number): void {
    this.http.get<TourLog[]>(`${this.apiBase}/${tourId}/logs`).subscribe(logs => {
      this._logs.set(logs);
    });
    // refresh the gallery whenever we load a tour's logs
    this.loadImagesForTour(tourId);
  }

  // fetch the list of image file names and turn them into full urls the browser can show
  loadImagesForTour(tourId: number): void {
    this.http.get<string[]>(`${this.apiBase}/${tourId}/images`).subscribe(names => {
      const urls = names.map(name => `http://localhost:8080/images/${name}`);
      this._images.set(urls);
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

  // send the chosen image file to the backend for a given log.
  // the file goes as multipart form data under the name "file".
  uploadImage(tourId: number, logId: number, file: File): void {
    const formData = new FormData();
    formData.append('file', file);
    this.http.post<TourLog>(`${this.apiBase}/${tourId}/logs/${logId}/image`, formData)
      .subscribe(() => {
        // once uploaded, refresh logs and the gallery so the new picture shows
        this.loadForTour(tourId);
      });
  }
}
