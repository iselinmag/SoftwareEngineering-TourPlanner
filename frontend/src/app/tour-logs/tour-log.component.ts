// Import Angular core functionality
import { Component, inject } from '@angular/core';

// Import common Angular directives like *ngIf, *ngFor
import { CommonModule } from '@angular/common';

// Import template-driven forms (ngModel)
import { FormsModule } from '@angular/forms';

// Import the ViewModel that handles all logic and data for logs
import { TourLogViewmodel } from './tour-log-viewmodel';

// Import types (interface + enum)
import { Difficulty, TourLog } from '../models/tour-log.model';

// Import child component that displays each log as a card
import { LogCardComponent } from './log-card.component';



@Component({
  selector: 'app-tour-log', // HTML tag used to render this component
  standalone: true, // this component does not need a module
  imports: [CommonModule, FormsModule, LogCardComponent], // what this component depends on
  templateUrl: './tour-log.component.html',
  styleUrl: './tour-log.component.css',
})
export class TourLogList {

  // Inject the shared ViewModel (this is where all data and logic lives)
  vm = inject(TourLogViewmodel);

  // Get all difficulty values (Easy, Medium, Hard) for dropdown
  difficulties = Object.values(Difficulty);

  // This is the form state (connected to inputs via ngModel)
  form = {
    id: '',              // used when editing an existing log
    dateTime: '',
    comment: '',
    difficulty: Difficulty.Easy,
    totalDistance: 0,
    totalTime: '',
    rating: 1
  };

  // Stores validation or error messages shown in UI
  errorMessage = '';

  // Tracks whether we are editing or creating a new log
  isEditing = false;


  // Called when user clicks "Add Log" or "Save Changes"
  saveLog() {
    // Reset previous error
    this.errorMessage = '';

    // Prevent creating logs without selecting a tour first
    if (!this.vm.selectedTourId()) {
      this.errorMessage = 'Please select a tour first.';
      return;
    }

    // Create a new log object based on form data
    const log: TourLog = {
      // If editing → keep existing ID, else generate new one
      id: this.isEditing ? this.form.id : Date.now().toString(),

      // Link log to currently selected tour
      tourId: this.vm.selectedTourId()!,

      // Map form values into log object
      dateTime: this.form.dateTime,
      comment: this.form.comment.trim(), // remove extra spaces
      difficulty: this.form.difficulty,
      totalDistance: Number(this.form.totalDistance), // ensure number
      totalTime: this.form.totalTime,
      rating: Number(this.form.rating)
    };

    // Validate input before saving
    if (!this.isFormValid(log)) {
      this.errorMessage =
        'Please fill all required fields correctly (rating 1–5, comment, date).';
      return;
    }

    // If editing → update existing log
    if (this.isEditing) {
      this.vm.updateLog(log);
    } 
    // Else → create new log
    else {
      this.vm.addLog(log);
    }

    // Reset form after saving
    this.resetForm();
  }


  // Called when user clicks "Edit" on a log card
  editLog(log: TourLog) {

    // Fill form with selected log values
    this.form = {
      id: log.id ?? '', // fallback if id is undefined
      dateTime: String(log.dateTime),
      comment: log.comment,
      difficulty: log.difficulty,
      totalDistance: log.totalDistance,
      totalTime: log.totalTime,
      rating: log.rating
    };

    // Switch UI into edit mode
    this.isEditing = true;

    // Clear any previous error messages
    this.errorMessage = '';
  }


  // Called when user clicks "Delete"
  deleteLog(id: string) {
    // Tell ViewModel to remove log
    this.vm.deleteLog(id);
  }


  // Called when user clicks "Cancel" while editing
  cancelEdit() {
    this.resetForm();
  }


  // Reset form to default values
  private resetForm() {
    this.form = {
      id: '',
      dateTime: '',
      comment: '',
      difficulty: Difficulty.Easy,
      totalDistance: 0,
      totalTime: '',
      rating: 1
    };

    // Exit edit mode
    this.isEditing = false;

    // Clear error messages
    this.errorMessage = '';
  }


  // Simple validation logic for form input
  private isFormValid(log: TourLog): boolean {
    return (
      !!log.dateTime &&              // must have date
      !!log.comment &&               // must have comment
      log.rating >= 1 &&             // rating between 1–5
      log.rating <= 5 &&
      log.totalDistance >= 0         // distance cannot be negative
    );
  }
}