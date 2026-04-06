import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourLog } from '../models/tour-log.model';

@Component({
  selector: 'app-log-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './log-card.component.html',
  styleUrl: './log-card.component.css',
})
export class LogCardComponent {
  @Input() log!: TourLog;

  @Output() edit = new EventEmitter<TourLog>();
  @Output() delete = new EventEmitter<string>();

  onEdit() {
    this.edit.emit(this.log);
  }

  onDelete() {
    if (this.log.id) {
      this.delete.emit(this.log.id);
    }
  }
}