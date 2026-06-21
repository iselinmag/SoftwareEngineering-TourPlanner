import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TourList } from '../tour-list/tour-list.component';
import { TourDetails } from '../tour-details/tour-details.component';
import { TourLogList } from '../tour-logs/tour-log.component';
import { TourMapComponent } from '../tour-map/tour-map.component';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, TourList, TourDetails, TourLogList, TourMapComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  private auth = inject(AuthService);

  // called when the user clicks the logout button in the header
  logout() {
    this.auth.logout();
  }
}
