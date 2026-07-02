import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourLog } from '../models/tour-log.model';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { TourLogService } from '../services/tour-log.service';

// this is one single log card, the little box that shows one log.
// it is used over and over, once per log, by the logs screen above it. it shows the log and
// its edit, delete and add image buttons. it does not do the work itself: when a button is
// pressed it just tells its parent screen, like passing a note up to whoever is in charge.
@Component({
  selector: 'app-log-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './log-card.component.html',
  styleUrl: './log-card.component.css',
})
export class LogCardComponent {
  auth = inject(AuthService);
  private logService = inject(TourLogService);

  // the one log this card should show, handed in by the parent screen
  @Input() log!: TourLog;

  // the note we pass up when the user wants to edit, carrying the whole log
  @Output() edit = new EventEmitter<TourLog>();

  // the note we pass up when the user wants to delete, carrying just the log id
  @Output() delete = new EventEmitter<number>();


  // edit button pressed: pass the log up to the parent to handle
  onEdit() {
    this.edit.emit(this.log);
  }


  // delete button pressed: pass the id up, but only if the log actually has one
  onDelete() {
    if (this.log.id !== undefined) {
      this.delete.emit(this.log.id);
    }
  }

  // called when the user picks an image file for this log.
  // the log already exists so it has an id, we just send the file to it.
  onImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (this.log.id === undefined) return;

    this.logService.uploadImage(this.log.tourId, this.log.id, file);
  }
}
