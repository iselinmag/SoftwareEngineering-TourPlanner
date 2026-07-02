import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TourList } from '../tour-list/tour-list.component';
import { TourDetails } from '../tour-details/tour-details.component';
import { TourLogList } from '../tour-logs/tour-log.component';
import { TourMapComponent } from '../tour-map/tour-map.component';
import { AuthService } from '../services/auth.service';
import { TourImagesComponent } from '../tour-images/tour-images.component';

// this is the main screen you land on after logging in.
// it is the dashboard that pulls all the smaller pieces together: the tour list, the details,
// the logs, the map and the image gallery. it does little work itself, it just lays them out.
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, TourList, TourDetails, TourLogList, TourMapComponent, TourImagesComponent],
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
