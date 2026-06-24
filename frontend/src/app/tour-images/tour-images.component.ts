import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourLogService } from '../services/tour-log.service';

@Component({
  selector: 'app-tour-images',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tour-images.component.html',
  styleUrl: './tour-images.component.css',
})
export class TourImagesComponent {

  private logService = inject(TourLogService);

  // the list of image urls for the current tour, comes straight from the service
  images = this.logService.images;

  // which image is showing right now
  currentIndex = signal(0);

  // the url of the image currently on screen
  currentImage = computed(() => {
    const list = this.images();
    if (list.length === 0) return null;
    // keep the index in range in case the list changed
    const safeIndex = this.currentIndex() % list.length;
    return list[safeIndex];
  });

  // go to the previous image, wrapping around to the end
  prev() {
    const count = this.images().length;
    if (count === 0) return;
    this.currentIndex.update(i => (i - 1 + count) % count);
  }

  // go to the next image, wrapping around to the start
  next() {
    const count = this.images().length;
    if (count === 0) return;
    this.currentIndex.update(i => (i + 1) % count);
  }
}
