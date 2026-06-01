import { Component, OnDestroy, effect, inject, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import { TourListViewmodel } from '../tour-list/tour-list-viewmodel';

const defaultIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
});

const endIcon = L.icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
});

@Component({
  selector: 'app-tour-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tour-map.component.html',
  styleUrl: './tour-map.component.css',
})
export class TourMapComponent implements AfterViewInit, OnDestroy {

  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef;

  private vm = inject(TourListViewmodel);
  private map: L.Map | null = null;
  private routeLayer: L.Polyline | null = null;
  private startMarker: L.Marker | null = null;
  private endMarker: L.Marker | null = null;
  private resizeObserver: ResizeObserver | null = null;

  constructor() {
    effect(() => {
        const tour = this.vm.selectedTour();
        if (this.map) {
        // tell leaflet the container may have resized so it remeasures everything
        this.map.invalidateSize(false);
        this.drawRoute(
            tour?.routeInformation ?? null,
            tour?.fromLocation,
            tour?.toLocation
        );
        }
    });
  }

  ngAfterViewInit() {
    // we wait for the container to have a non-zero size before creating the map
    // this solves the tile misalignment problem permanently
    this.waitForSize(this.mapContainer.nativeElement, () => {
      this.initMap();
    });
  }

  // keeps checking the container dimensions every 50ms
  // only calls the callback once the container has real width and height
  // this is more reliable than any fixed setTimeout because it reacts to
  // actual layout completion rather than guessing a time
  private waitForSize(el: HTMLElement, callback: () => void) {
    const check = () => {
      if (el.offsetWidth > 0 && el.offsetHeight > 0) {
        callback();
      } else {
        setTimeout(check, 50);
      }
    };
    check();
  }

  private initMap() {
    const container = this.mapContainer.nativeElement;

    this.map = L.map(container, {
      zoomAnimation: false,
      fadeAnimation: false,
      markerZoomAnimation: false,
    }).setView([48.2082, 16.3738], 12);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);

    // Force Leaflet to re-measure the container after the current JS task completes.
    // Even though waitForSize confirmed a non-zero size, the browser may not have
    // finished its layout pass yet, so Leaflet's initial tile positioning can be off.
    setTimeout(() => this.map?.invalidateSize(), 0);

    // Re-measure whenever the card is resized (e.g. window resize, sidebar toggle).
    this.resizeObserver = new ResizeObserver(() => this.map?.invalidateSize());
    this.resizeObserver.observe(container);

    // draw the current tour route if one is already selected when the map loads
    const tour = this.vm.selectedTour();
    if (tour?.routeInformation) {
      this.drawRoute(tour.routeInformation, tour.fromLocation, tour.toLocation);
    }
  }

  ngOnDestroy() {
    this.resizeObserver?.disconnect();
    this.resizeObserver = null;
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  private drawRoute(geometryJson: string | null, from?: string, to?: string) {
    if (!this.map) return;
    this.clearRoute();
    if (!geometryJson) return;

    try {
      const coords: number[][] = JSON.parse(geometryJson);
      const latLngs: L.LatLng[] = coords.map(([lon, lat]) => L.latLng(lat, lon));
      if (latLngs.length === 0) return;

      this.routeLayer = L.polyline(latLngs, {
        color: '#3b82f6',
        weight: 5,
        opacity: 0.8,
      }).addTo(this.map);

      this.startMarker = L.marker(latLngs[0], { icon: defaultIcon })
        .bindPopup(from ?? 'Start')
        .addTo(this.map);

      this.endMarker = L.marker(latLngs[latLngs.length - 1], { icon: endIcon })
        .bindPopup(to ?? 'End')
        .addTo(this.map);

      this.map.fitBounds(this.routeLayer.getBounds(), { padding: [40, 40], animate: false });

    } catch (e) {
      console.error('failed to parse route geometry', e);
    }
  }

  private clearRoute() {
    if (!this.map) return;
    if (this.routeLayer) { this.map.removeLayer(this.routeLayer); this.routeLayer = null; }
    if (this.startMarker) { this.map.removeLayer(this.startMarker); this.startMarker = null; }
    if (this.endMarker) { this.map.removeLayer(this.endMarker); this.endMarker = null; }
  }
}