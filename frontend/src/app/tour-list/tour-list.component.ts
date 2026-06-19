
// <Matej

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourLogViewmodel } from '../tour-logs/tour-log-viewmodel';
import { TourListViewmodel } from '../tour-list/tour-list-viewmodel';
import { Tour, TransportType } from '../models/tour.model';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-tour-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tour-list.component.html',
  styleUrl: './tour-list.component.css',
})
export class TourList {
  // Inject our ViewModels
  // inject() asks Angular to provide the shared singleton instance (meaning instead of copying the viewmodel and creating duplicates it just uses 1 instead) of each service.
  listvm = inject(TourListViewmodel);
  logVm = inject(TourLogViewmodel);

  // Hide the form by default, toggled by clicking the form header
  isFormVisible = false

  // FormBuilder is an Angular utility that makes building reactive forms easier.
  private fb = inject(FormBuilder) // we use it for Creating and updating tours

  // Create form structure
  tourForm = this.fb.group({
    id: [''],
    name: ['', [Validators.required, Validators.minLength(3)]],// QUESTION: What is Validator?
    description: ['', Validators.required],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    transportType: [TransportType.Walk, Validators.required],
    distance: [0],
    estimatedTime: ['']
  });

  transportTypes = Object.values(TransportType);

  private tourImageMap: Record<TransportType, string> = {
    [TransportType.Walk]: 'assets/walk-tour.png',
    [TransportType.Hike]: 'assets/hike-tour.png',
    [TransportType.Bike]: 'assets/bike-tour.png',
    [TransportType.Car]:  'assets/drive-tour.png',
    [TransportType.Run]:  'assets/run-tour.png',
    [TransportType.Boat]: 'assets/drive-tour.png',
  };

  getTourImage(type: TransportType): string {
    return this.tourImageMap[type] ?? 'assets/walk-tour.png';
  }

  // Called when a tour card is clicked.
  // It updates both ViewModels so TourDetails and TourLog stay in sync.
  onTourClick(tour: Tour) {
    if (tour.id) {
      this.listvm.selectTour(tour.id); // update list view
      this.logVm.setSelectedTour(tour.id); // update log view

      // Logic to automatically scroll to have map full screen when selected tour on mobile devices
      const mapDetailsSection = document.getElementById('mapDetailsSection');
      const mobileBreakpoint = 900; 

      if (mapDetailsSection && window.innerWidth <= mobileBreakpoint) {
        mapDetailsSection.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }

  // called when user clicks create tour
  // sends data to viewmodel then resets form
  addTour() {
    if (this.tourForm.invalid) return;
    const tourData = this.tourForm.value as Tour;
    this.listvm.addTour(tourData, () => {
      // Only reset the form after the backend confirms the tour was saved
      this.tourForm.reset({ transportType: TransportType.Walk, distance: 0 });
    });
  }


onImportFileSelected(event: Event): void {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];

  if (!file) return;

  this.listvm.importTours(file);

  // Reset input so the same file can be selected again later
  input.value = '';
}
}