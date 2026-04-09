
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
  listvm = inject(TourListViewmodel);
  logVm = inject(TourLogViewmodel);

  // Hide the form by default
  isFormVisible = false

  private fb = inject(FormBuilder) // we use it for Creating and updating tours

  // Create form structure
  tourForm = this.fb.group({
    id: [''],
    name: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', Validators.required],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    transportType: [TransportType.Bike, Validators.required],
    distance: [0, [Validators.required, Validators.min(0.1)]],
    estimatedTime: ['', Validators.required]
  });

  transportTypes = Object.values(TransportType);

  onTourClick(tour: Tour) {
    if (tour.id) {
      this.listvm.selectTour(tour.id); // update list view
      this.logVm.setSelectedTour(tour.id); // update log view

      // Logic to automatically scroll to have map full screen when selected tour on mobile devices
      // 1. Get a reference to the Map and Details section you want to scroll to.
      const mapDetailsSection = document.getElementById('mapDetailsSection');

      // 2. Define mobile breakpoint
      const mobileBreakpoint = 900; 

      // 3. Check screen width AND if the section exists.
      if (mapDetailsSection && window.innerWidth <= mobileBreakpoint) {
        // 4. Automatically and smoothly scroll into view.
        mapDetailsSection.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }

  addTour() {
    if (this.tourForm.invalid) return;
    const tourData = this.tourForm.value as Tour;
    this.listvm.addTour(tourData); // Only Adds
    this.tourForm.reset({ transportType: TransportType.Bike, distance: 0 }); // Reset form
  }
}