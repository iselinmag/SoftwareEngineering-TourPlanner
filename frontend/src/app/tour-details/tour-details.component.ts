import { Component, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourListViewmodel } from '../tour-list/tour-list-viewmodel';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Tour, TransportType } from '../models/tour.model';
import { AuthService } from '../services/auth.service';

// this is the tour details panel shown in the middle when you pick a tour.
// it shows the chosen tour's info, and if the tour is yours it lets you switch into edit mode
// to change or delete it. it borrows the picked tour from the shared list helper.
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

  isEditMode = false;   // are we currently editing, or just looking?
  isSaving = false;
  transportTypes = Object.values(TransportType);
  auth = inject(AuthService);

  // the shape of the edit form, one line per box with its starting value and rules.
  // the id starts as nothing because the form fills it in from the tour being edited.
  editForm = this.fb.group({
    id: [null as number | null],
    name: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', Validators.required],
    fromLocation: ['', Validators.required],
    toLocation: ['', Validators.required],
    transportType: [TransportType.Walk, Validators.required],
    distance: [0],
    estimatedTime: [''],
    routeInformation: ['']
  });

  constructor() {
    // if the user clicks a different tour while editing, drop out of edit mode automatically.
    // effect runs this again every time the picked tour changes, like a little tripwire.
    effect(() => {
      this.vm.selectedTour();
      this.isEditMode = false;
    });
  }

  // turn the friendliness text into a short colour label used by the styling.
  // "child friendly" becomes good (green), "moderate" becomes ok, anything else becomes bad.
  getFriendlinessClass(value: string | undefined): string {
    if (!value) return 'bad';
    if (value === 'Child Friendly') return 'good';
    if (value === 'Moderate') return 'ok';
    return 'bad';
  }

  // switch into edit mode and pre fill the form with the tour's current details
  enableEditMode(tour: Tour) {
    this.isEditMode = true;
    this.editForm.patchValue(tour);
  }

  // back out of editing without saving
  cancelEdit() {
    this.isEditMode = false;
  }

  // save the edited tour.
  // we hand the changes to the shared helper. the backend works out the new distance and time
  // from the route, and when its reply comes back the shared list swaps in the updated tour,
  // so the screen shows the fresh values on its own without us setting them here.
  saveEdit() {
    if (this.editForm.invalid) return;
    this.isSaving = true;

    this.vm.updateTour(this.editForm.value as Tour);

    this.isEditMode = false;
    this.isSaving = false;
  }

  // delete the tour, but ask for a yes first so it does not happen by accident
  deleteTour(id: number) {
    if (confirm('Are you sure you want to delete this tour?')) {
      this.vm.deleteTour(id);
    }
  }
}