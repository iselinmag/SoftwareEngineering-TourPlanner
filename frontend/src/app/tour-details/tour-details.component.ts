// Matej

import { Component, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourListViewmodel } from '../tour-list/tour-list-viewmodel';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Tour, TransportType } from '../models/tour.model';

@Component({
  selector: 'app-tour-details',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tour-details.component.html',
  styleUrl: './tour-details.component.css',
})
export class TourDetails {
  vm = inject(TourListViewmodel);

  private fb = inject(FormBuilder);

  isEditMode = false;
  transportTypes = Object.values(TransportType);

  editForm = this.fb.group({
    id: [''],
    name: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', Validators.required],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    transportType: [TransportType.Bike, Validators.required],
    distance: [0, [Validators.required, Validators.min(0.1)]],
    estimatedTime: ['', Validators.required]
  });

  constructor() {
    // Automatically exit edit mode if the user clicks on a diffrent tour in the list
    effect(() => {
      const currentTour = this.vm.selectedTour();
      this.isEditMode = false; 
    });
  }

  // Puts the selected tour's data into the form and shows it
  enableEditMode(tour: Tour) {
    this.isEditMode = true;
    this.editForm.patchValue(tour);
  }

  cancelEdit() {
    this.isEditMode = false;
  }

  saveEdit() {
    if (this.editForm.invalid) return;
    this.vm.updateTour(this.editForm.value as Tour);
    this.isEditMode = false; // return to normal view
  }

  deleteTour(id: string) {
    // Add a simple confirmation so they don't accidentally click it
    if(confirm('Are you sure you want to delete this tour?')) {
      this.vm.deleteTour(id);
    }
  }
}
