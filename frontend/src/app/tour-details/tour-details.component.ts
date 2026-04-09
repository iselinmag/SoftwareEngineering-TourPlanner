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
  // Inject the shared ViewModel to read the currently selected tour and to update/delete tours from it
  vm = inject(TourListViewmodel);

  private fb = inject(FormBuilder);

  isEditMode = false;
  transportTypes = Object.values(TransportType);

  // Defines the edit form structure with the same structure and rules as the create form
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
    // Automatically exit edit mode if the user clicks on a different tour in the list
    // effect() runs this code every time selectedTour() changes.
    // we use it to prevent bug where user clicks edit on tour A but switches to tour B, we want edit form to reset
    effect(() => {
      const currentTour = this.vm.selectedTour(); // reading this signal registers it as a dependency
      this.isEditMode = false; 
    });
  }

  // Switches to edit mode and puts the selected tour's data into the form and shows it
  enableEditMode(tour: Tour) {
    this.isEditMode = true;
    this.editForm.patchValue(tour); // patchValue() fills each form field with the matching property from the tour object.
  }

  cancelEdit() {
    this.isEditMode = false;
  }

  // Sends the updated tour data to the ViewModel and disables edit mode
  saveEdit() {
    if (this.editForm.invalid) return;
    this.vm.updateTour(this.editForm.value as Tour);
    this.isEditMode = false; // return to normal view
  }

  deleteTour(id: string) {
    // We ask for confirmation before deleting to prevent accidental clicks.
    if(confirm('Are you sure you want to delete this tour?')) {
      this.vm.deleteTour(id);
    }
  }
}
