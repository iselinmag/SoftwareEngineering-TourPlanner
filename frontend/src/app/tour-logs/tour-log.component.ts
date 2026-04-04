import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TourLogViewmodel } from './tour-log-viewmodel';
import { Difficulty, TourLog } from '../models/tour-log.model';

@Component({
  selector: 'app-tour-log',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tour-log.component.html',
  styleUrl: './tour-log.component.css',
})
export class TourLogList {
  vm = inject(TourLogViewmodel);

  difficulties = Object.values(Difficulty);

  form = {
    id: '',
    dateTime: '',
    comment: '',
    difficulty: Difficulty.Easy,
    totalDistance: 0,
    totalTime: '',
    rating: 1
  };

  errorMessage = '';
  isEditing = false;

  saveLog() {
    this.errorMessage = '';

    // ❗ viktig UX: må velge tour først
    if (!this.vm.selectedTourId()) {
      this.errorMessage = 'Please select a tour first.';
      return;
    }

    // ❗ trim + fallback
    const comment = this.form.comment.trim();

    const log: TourLog = {
      id: this.isEditing ? this.form.id : Date.now().toString(),
      tourId: this.vm.selectedTourId()!,
      dateTime: this.form.dateTime,
      comment: comment,
      difficulty: this.form.difficulty,
      totalDistance: Number(this.form.totalDistance),
      totalTime: this.form.totalTime,
      rating: Number(this.form.rating)
    };

    // ❗ ekstra sikker validering
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

  editLog(log: TourLog) {
    this.form = {
      id: log.id ?? '',
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

  cancelEdit() {
    this.resetForm();
  }

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

    this.isEditing = false;
    this.errorMessage = '';
  }

  // 🔥 bedre validering (sensor liker dette)
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