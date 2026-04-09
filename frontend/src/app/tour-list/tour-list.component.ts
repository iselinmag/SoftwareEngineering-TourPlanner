
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
    transportType: [TransportType.Bike, Validators.required],
    distance: [0, [Validators.required, Validators.min(0.1)]],
    estimatedTime: ['', Validators.required]
  });

  transportTypes = Object.values(TransportType);

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
    this.listvm.addTour(tourData);
    this.tourForm.reset({ transportType: TransportType.Bike, distance: 0 }); // Reset form
  }
}