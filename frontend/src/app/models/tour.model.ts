import { TourLog } from './tour-log.model';

// this describes the shape of a tour on the frontend side.
// it is just a plain list of the fields a tour has, so the rest of the app knows what to
// expect when it reads or builds one. it mirrors the tour shape the backend sends back.

// the short fixed menu of travel types a tour can use
export enum TransportType {
  Walk = 'Walk',
  Hike = 'Hike',
  Bike = 'Bike',
  Car = 'Car',
  Boat = 'Boat',
  Run = 'Run'
}

export interface Tour {
  id?: number;
  ownerUsername?: string;

  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  transportType: TransportType;

  distance: number;
  estimatedTime: string;
  routeInformation?: string;

  popularity?: number;
  popularityLevel?: string;
  childFriendliness?: string;

  // only filled in when tours are exported or imported as a file, empty the rest of the time
  tourLogs?: TourLog[];
}