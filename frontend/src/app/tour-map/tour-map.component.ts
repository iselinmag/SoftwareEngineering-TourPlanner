import { Component, OnDestroy, effect, inject, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import { TourListViewmodel } from '../tour-list/tour-list-viewmodel';

// this is the map panel that draws the chosen tour's route.
// it uses leaflet (a ready made map library) to show a real world map, then draws a blue line
// for the route with a pin at the start and a red pin at the end. when you pick a different
// tour it wipes the old line and draws the new one.

// the two pin pictures: a normal one for the start, a red one for the end
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
    // watch for the picked tour changing. every time it does, redraw the route for the new one.
    // effect is like a tripwire that runs this again whenever the tour it reads changes.
    effect(() => {
        const tour = this.vm.selectedTour();
        if (this.map) {
        // tell leaflet the map area may have changed size so it measures itself again
        this.map.invalidateSize(false);
        this.drawRoute(
            tour?.routeInformation ?? null,
            tour?.fromLocation,
            tour?.toLocation
        );
        }
    });
  }

  // runs once the panel is on screen.
  // we hold off building the map until its box actually has a size, otherwise the map tiles
  // line up wrong. so we wait for a real size first, then build.
  ngAfterViewInit() {
    this.waitForSize(this.mapContainer.nativeElement, () => {
      this.initMap();
    });
  }

  // keep peeking at the box every so often, and only run the given step once the box has a real
  // width and height. this is safer than just waiting a guessed amount of time, because it
  // reacts to the page actually being ready rather than hoping it is ready by then.
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

  // build the actual map: create it, add the map picture tiles, and draw the route if one is
  // already picked. it also watches for the box changing size so the map keeps lining up.
  private initMap() {
    const container = this.mapContainer.nativeElement;

    this.map = L.map(container, {
      zoomAnimation: false,
      fadeAnimation: false,
      markerZoomAnimation: false,
    }).setView([20, 0], 2);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);

    // nudge the map to measure itself once more right after this runs. even though we waited
    // for a real size, the page may not have fully settled yet, so this catches any last shift.
    setTimeout(() => this.map?.invalidateSize(), 0);

    // whenever the map box changes size later (window resize, sidebar opening), measure again
    this.resizeObserver = new ResizeObserver(() => this.map?.invalidateSize());
    this.resizeObserver.observe(container);

    // if a tour is already picked when the map first loads, draw its route straight away
    const tour = this.vm.selectedTour();
    if (tour?.routeInformation) {
      this.drawRoute(tour.routeInformation, tour.fromLocation, tour.toLocation);
    }
  }

  // runs when the panel is closed. tidy up the map and the size watcher so nothing keeps
  // running in the background and wasting memory after we are gone.
  ngOnDestroy() {
    this.resizeObserver?.disconnect();
    this.resizeObserver = null;
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  // draw a tour's route on the map.
  // step by step: wipe any old route, unpack the saved list of points, join them into a blue
  // line, drop a pin at the start and end, then zoom the map so the whole route fits on screen.
  private drawRoute(geometryJson: string | null, from?: string, to?: string) {
    if (!this.map) return;
    this.clearRoute();
    if (!geometryJson) return;

    try {
      // the route was saved as text, so we unpack it back into a list of points.
      // each point is stored as longitude then latitude, and leaflet wants them the other way,
      // so we flip them here.
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

  // rub out the current route and its two pins, so we have a clean map before drawing a new one
  private clearRoute() {
    if (!this.map) return;
    if (this.routeLayer) { this.map.removeLayer(this.routeLayer); this.routeLayer = null; }
    if (this.startMarker) { this.map.removeLayer(this.startMarker); this.startMarker = null; }
    if (this.endMarker) { this.map.removeLayer(this.endMarker); this.endMarker = null; }
  }
}