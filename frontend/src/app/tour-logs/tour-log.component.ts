import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TourLogViewmodel } from './tour-log-viewmodel';
import { Difficulty, TourLog } from '../models/tour-log.model';
import { LogCardComponent } from './log-card.component';

// this is the tour logs screen for the chosen tour.
// it holds the form for adding or editing a log, and shows the existing logs as cards below.
// the same form does double duty: empty for a new log, or pre filled when editing an old one.
@Component({
  selector: 'app-tour-log',
  standalone: true,
  imports: [CommonModule, FormsModule, LogCardComponent],
  templateUrl: './tour-log.component.html',
  styleUrl: './tour-log.component.css',
})
export class TourLogList {

  vm = inject(TourLogViewmodel);

  difficulties = Object.values(Difficulty);

  // the boxes in the log form and their starting values.
  // the id is empty (null) for a new log, and holds the log's id when we are editing one.
  form: {
    id: number | null;
    dateTime: string;
    comment: string;
    difficulty: Difficulty;
    totalDistance: number;
    totalTime: string;
    rating: number;
  } = {
    id: null,
    dateTime: '',
    comment: '',
    difficulty: Difficulty.Easy,
    totalDistance: 0,
    totalTime: '',
    rating: 1
  };

  errorMessage = '';
  isEditing = false;   // are we editing an existing log, or making a new one?

  // runs when the add log or save changes button is pressed.
  // step by step: make sure a tour is picked, build a log from the form, check it follows the
  // rules, then either save the changes or add it as new, and finally clear the form.
  saveLog() {
    this.errorMessage = '';

    if (!this.vm.selectedTourId()) {
      this.errorMessage = 'Please select a tour first.';
      return;
    }

    // build the log from the form. for a brand new log we leave the id empty and let the
    // backend hand out the real one, for an edit we keep the existing id.
    const log: TourLog = {
      id: this.isEditing && this.form.id !== null ? this.form.id : undefined,
      tourId: this.vm.selectedTourId()!,
      dateTime: this.form.dateTime,
      comment: this.form.comment.trim(),
      difficulty: this.form.difficulty,
      totalDistance: Number(this.form.totalDistance),
      totalTime: this.form.totalTime,
      rating: Number(this.form.rating)
    };

    if (!this.isFormValid(log)) {
      this.errorMessage =
        'Please fill all required fields correctly (rating 1 to 5, comment, date).';
      return;
    }

    if (this.isEditing) {
      this.vm.updateLog(log);
    } else {
      this.vm.addLog(log);
    }

    this.resetForm();
  }

  // runs when the edit button on a log card is clicked.
  // it copies that log's details into the form and switches the form into editing mode.
  editLog(log: TourLog) {
    this.form = {
      id: log.id ?? null,
      dateTime: String(log.dateTime),
      comment: log.comment,
      difficulty: log.difficulty,
      totalDistance: log.totalDistance,
      totalTime: log.totalTime,
      rating: log.rating
    };
    this.isEditing = true;
    this.errorMessage = '';
  }

  // runs when the delete button on a log card is clicked.
  // deleting needs to know both the log and which tour it is under, so we pass both along.
  deleteLog(id: number) {
    const tourId = this.vm.selectedTourId();
    if (tourId !== null) {
      this.vm.deleteLog(id, tourId);
    }
  }

  // back out of editing and empty the form
  cancelEdit() {
    this.resetForm();
  }

  // wipe the form back to blank and leave editing mode
  private resetForm() {
    this.form = {
      id: null,
      dateTime: '',
      comment: '',
      difficulty: Difficulty.Easy,
      totalDistance: 0,
      totalTime: '',
      rating: 1
    };
    this.isEditing = false;
    this.errorMessage = '';
  }

  // check the log follows the basic rules before we try to save it:
  // it needs a date, a comment, a rating between 1 and 5, and a distance that is not negative
  private isFormValid(log: TourLog): boolean {
    return (
      !!log.dateTime &&
      !!log.comment &&
      log.rating >= 1 &&
      log.rating <= 5 &&
      log.totalDistance >= 0
    );
  }
}