import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourLog } from '../models/tour-log.model';

@Component({
  selector: 'app-log-card', // HTML tag used to display this component
  standalone: true, // no Angular module needed
  imports: [CommonModule], // enables *ngIf, *ngFor, etc.
  templateUrl: './log-card.component.html',
  styleUrl: './log-card.component.css',
})
export class LogCardComponent {

  // Receives a single log object from the parent component
  // The "!" tells TypeScript this value will always be provided
  @Input() log!: TourLog;

  // Emits an event when the user wants to edit this log
  // Sends the full log object back to the parent
  @Output() edit = new EventEmitter<TourLog>();

  // Emits an event when the user wants to delete this log
  // Sends only the log ID back to the parent
  @Output() delete = new EventEmitter<string>();


  // Called when user clicks the "Edit" button
  onEdit() {
    // Send the current log to parent component
    this.edit.emit(this.log);
  }


  // Called when user clicks the "Delete" button
  onDelete() {
    // Only send if log has an ID (safety check)
    if (this.log.id) {
      this.delete.emit(this.log.id);
    }
  }
}