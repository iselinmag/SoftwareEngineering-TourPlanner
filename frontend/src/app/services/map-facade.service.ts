import { Injectable } from '@angular/core';
import * as L from 'leaflet';

// Facade around Leaflet.
// The component should not know Leaflet details such as markers, tile layers,
// polylines or map cleanup. It only asks this service to show or clear a route.
@Injectable({
  providedIn: 'root',
})
export class MapFacadeService {
  private map: L.Map | null = null;
  private routeLayer: L.Polyline | null = null;
  private startMarker: L.Marker | null = null;
  private endMarker: L.Marker | null = null;
  private resizeObserver: ResizeObserver | null = null;

  private readonly defaultIcon = L.icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
  });

  private readonly endIcon = L.icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
  });

  initMap(container: HTMLElement): void {
    if (this.map) {
      return;
    }

    this.map = L.map(container, {
      zoomAnimation: false,
      fadeAnimation: false,
      markerZoomAnimation: false,
    }).setView([20, 0], 2);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);

    setTimeout(() => this.map?.invalidateSize(), 0);

    this.resizeObserver = new ResizeObserver(() => this.map?.invalidateSize());
    this.resizeObserver.observe(container);
  }

  drawRoute(geometryJson: string | null, from?: string, to?: string): void {
    if (!this.map) {
      return;
    }

    this.clearRoute();

    if (!geometryJson) {
      return;
    }

    try {
      // OpenRouteService stores coordinates as [longitude, latitude].
      // Leaflet needs [latitude, longitude], so the order is flipped here.
      const coords: number[][] = JSON.parse(geometryJson);
      const latLngs: L.LatLng[] = coords.map(([lon, lat]) => L.latLng(lat, lon));

      if (latLngs.length === 0) {
        return;
      }

      this.routeLayer = L.polyline(latLngs, {
        color: '#3b82f6',
        weight: 5,
        opacity: 0.8,
      }).addTo(this.map);

      this.startMarker = L.marker(latLngs[0], { icon: this.defaultIcon })
        .bindPopup(from ?? 'Start')
        .addTo(this.map);

      const lastLatLng = latLngs.at(-1);

      if (!lastLatLng) {
        return;
      }

      this.endMarker = L.marker(lastLatLng, { icon: this.endIcon })
        .bindPopup(to ?? 'End')
        .addTo(this.map);

      this.map.fitBounds(this.routeLayer.getBounds(), {
        padding: [40, 40],
        animate: false,
      });
    } catch (error) {
      console.error('failed to parse route geometry', error);
    }
  }

  clearRoute(): void {
    if (!this.map) {
      return;
    }

    if (this.routeLayer) {
      this.map.removeLayer(this.routeLayer);
      this.routeLayer = null;
    }

    if (this.startMarker) {
      this.map.removeLayer(this.startMarker);
      this.startMarker = null;
    }

    if (this.endMarker) {
      this.map.removeLayer(this.endMarker);
      this.endMarker = null;
    }
  }

  invalidateSize(): void {
    this.map?.invalidateSize(false);
  }

  destroyMap(): void {
    this.resizeObserver?.disconnect();
    this.resizeObserver = null;

    if (this.map) {
      this.clearRoute();
      this.map.remove();
      this.map = null;
    }
  }
}