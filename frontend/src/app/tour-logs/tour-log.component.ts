import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TourLogViewmodel } from './tour-log-viewmodel';
import { Difficulty, TourLog } from '../models/tour-log.model';
import { LogCardComponent } from './log-card.component';

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

  // FIX: id is now number | null to match TourLog.id type (number).
  // We use null instead of '' as the empty/no-id state.
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
  isEditing = false;

  // Called when user clicks "Add Log" or "Save Changes"
  saveLog() {
    this.errorMessage = '';

    if (!this.vm.selectedTourId()) {
      this.errorMessage = 'Please select a tour first.';
      return;
    }

    // FIX: id is now number | undefined (not string).
    // When creating a new log we leave id undefined — the backend assigns the real ID.
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
        'Please fill all required fields correctly (rating 1–5, comment, date).';
      return;
    }

    if (this.isEditing) {
      this.vm.updateLog(log);
    } else {
      this.vm.addLog(log);
    }

    this.resetForm();
  }

  // Called when user clicks "Edit" on a log card
  editLog(log: TourLog) {
    this.form = {
      id: log.id ?? null,              // FIX: was log.id ?? '' — now number | null
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

  // FIX: deleteLog now receives a number and also passes tourId
  // because TourLogViewmodel.deleteLog(id, tourId) needs both.
  deleteLog(id: number) {
    const tourId = this.vm.selectedTourId();
    if (tourId !== null) {
      this.vm.deleteLog(id, tourId);
    }
  }

  cancelEdit() {
    this.resetForm();
  }

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