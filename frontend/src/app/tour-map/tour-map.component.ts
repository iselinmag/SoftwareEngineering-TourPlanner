import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourListViewmodel } from '../tour-list/tour-list-viewmodel';
import { MapFacadeService } from '../services/map-facade.service';

// This component only reacts to Angular state.
// Leaflet-specific logic is isolated in MapFacadeService.
@Component({
  selector: 'app-tour-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tour-map.component.html',
  styleUrl: './tour-map.component.css',
})
export class TourMapComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLElement>;

  private readonly vm = inject(TourListViewmodel);
  private readonly mapFacade = inject(MapFacadeService);

  constructor() {
    effect(() => {
      const tour = this.vm.selectedTour();

      this.mapFacade.invalidateSize();

      this.mapFacade.drawRoute(
        tour?.routeInformation ?? null,
        tour?.fromLocation,
        tour?.toLocation
      );
    });
  }

  ngAfterViewInit(): void {
    this.waitForSize(this.mapContainer.nativeElement, () => {
      this.mapFacade.initMap(this.mapContainer.nativeElement);

      const tour = this.vm.selectedTour();

      this.mapFacade.drawRoute(
        tour?.routeInformation ?? null,
        tour?.fromLocation,
        tour?.toLocation
      );
    });
  }

  private waitForSize(el: HTMLElement, callback: () => void): void {
    const check = () => {
      if (el.offsetWidth > 0 && el.offsetHeight > 0) {
        callback();
      } else {
        setTimeout(check, 50);
      }
    };

    check();
  }

  ngOnDestroy(): void {
    this.mapFacade.destroyMap();
  }
}