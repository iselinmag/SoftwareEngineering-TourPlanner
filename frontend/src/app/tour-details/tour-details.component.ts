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

  // The form uses null for the id field because Angular forms work with
  // null/string internally. We convert to number when we call the service.
  editForm = this.fb.group({
    id: [null as number | null],       // FIX: was [''] — id is a number, not a string
    name: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', Validators.required],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    transportType: [TransportType.Bike, Validators.required],
    distance: [0, [Validators.required, Validators.min(0.1)]],
    estimatedTime: ['', Validators.required]
  });

  constructor() {
    // Automatically exit edit mode if the user clicks a different tour.
    // effect() re-runs every time selectedTour() signal changes.
    effect(() => {
      this.vm.selectedTour();
      this.isEditMode = false;
    });
  }

  // converts the backend string into a safe css class name
  // "Child Friendly" -> "good", "Moderate" -> "ok", anything else -> "bad"
  getFriendlinessClass(value: string | undefined): string {
    if (!value) return 'bad';
    if (value === 'Child Friendly') return 'good';
    if (value === 'Moderate') return 'ok';
    return 'bad';
  }

  // Switches to edit mode and fills the form with the current tour's data.
  enableEditMode(tour: Tour) {
    this.isEditMode = true;
    this.editForm.patchValue(tour);    // FIX: works now because form id is number | null
  }

  cancelEdit() {
    this.isEditMode = false;
  }

  // Sends the updated tour to the ViewModel and returns to read-only view.
  saveEdit() {
    if (this.editForm.invalid) return;
    this.vm.updateTour(this.editForm.value as Tour);
    this.isEditMode = false;
  }

  // FIX: parameter type changed from string to number to match Tour.id type
  deleteTour(id: number) {
    if (confirm('Are you sure you want to delete this tour?')) {
      this.vm.deleteTour(id);
    }
  }
}