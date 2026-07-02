import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TourLog } from '../models/tour-log.model';

// this is the go between for tour logs and their images.
// like the tour service, it keeps a live list of the logs for the tour being viewed, plus a
// live list of that tour's images for the gallery. screens watch these lists and redraw when
// they change, so we never have to tell each screen by hand that something updated.
@Injectable({ providedIn: 'root' })
export class TourLogService {

  private http = inject(HttpClient);
  private apiBase = 'http://localhost:8080/api/tours';

  // the logs for the tour currently open, both the private version and the watchable view
  private _logs = signal<TourLog[]>([]);
  readonly logs = this._logs.asReadonly();

  // the full picture links for the current tour's gallery
  private _images = signal<string[]>([]);
  readonly images = this._images.asReadonly();

  // load the logs for one tour, and refresh its picture gallery at the same time
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

  // send a new log to the backend, then add it to the shared list
  add(log: TourLog): void {
    this.http.post<TourLog>(`${this.apiBase}/${log.tourId}/logs`, log).subscribe(created => {
      this._logs.update(current => [...current, created]);
    });
  }

  // save changes to a log, then swap the old copy in the list for the updated one
  update(log: TourLog): void {
    this.http.put<TourLog>(`${this.apiBase}/${log.tourId}/logs/${log.id}`, log).subscribe(updated => {
      this._logs.update(current =>
        current.map(l => l.id === updated.id ? updated : l)
      );
    });
  }

  // ask the backend to delete a log, then drop it from the list
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
