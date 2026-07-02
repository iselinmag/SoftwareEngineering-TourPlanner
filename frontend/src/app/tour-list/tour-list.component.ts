import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourLogViewmodel } from '../tour-logs/tour-log-viewmodel';
import { TourListViewmodel } from '../tour-list/tour-list-viewmodel';
import { Tour, TransportType } from '../models/tour.model';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

// this is the tour list screen on the left side.
// it shows every tour as a card, holds the search box, and holds the form for making a new
// tour. clicking a card tells the rest of the app which tour to show details and logs for.
@Component({
  selector: 'app-tour-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tour-list.component.html',
  styleUrl: './tour-list.component.css',
})
export class TourList {
  // grab the shared helpers that hold the tour list and log state.
  // inject asks angular for the one shared copy, so every screen sees the same data
  // instead of each making its own separate copy.
  listvm = inject(TourListViewmodel);
  logVm = inject(TourLogViewmodel);

  // the new tour form starts hidden, clicking its header opens and closes it
  isFormVisible = false

  // a little angular helper that makes building forms less fiddly, used for the create form
  private fb = inject(FormBuilder)

  // the shape of the create tour form: each line is one box, with its starting value and rules.
  // a validator is just a rule the box must pass, like "this cannot be empty" or "at least 3 letters".
  tourForm = this.fb.group({
    id: [''],
    name: ['', [Validators.required, Validators.minLength(3)]],
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

  // pick the right picture for a tour based on its travel type, falling back to the walk one
  getTourImage(type: TransportType): string {
    return this.tourImageMap[type] ?? 'assets/walk-tour.png';
  }

  // runs when a tour card is clicked.
  // it tells both shared helpers which tour is now chosen, so the details and logs update too.
  // on a small phone screen it also slides the map and details into view so you do not have to scroll.
  onTourClick(tour: Tour) {
    if (tour.id) {
      this.listvm.selectTour(tour.id);
      this.logVm.setSelectedTour(tour.id);

      const mapDetailsSection = document.getElementById('mapDetailsSection');
      const mobileBreakpoint = 900;

      if (mapDetailsSection && window.innerWidth <= mobileBreakpoint) {
        mapDetailsSection.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }

  // runs when the create button is pressed.
  // if the form breaks a rule we stop, otherwise we send the new tour off and only clear the
  // form once the backend confirms it was saved, so nothing is lost if saving fails.
  addTour() {
    if (this.tourForm.invalid) return;
    const tourData = this.tourForm.value as Tour;
    this.listvm.addTour(tourData, () => {
      this.tourForm.reset({ transportType: TransportType.Walk, distance: 0 });
    });
  }

  // runs when the user picks a file to import.
  // it hands the chosen file off to be loaded, then clears the picker so the same file can be
  // chosen again later if needed.
  onImportFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) return;

    this.listvm.importTours(file);

    input.value = '';
  }
}