import { TourLog } from './tour-log.model';

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

  // Included when exporting/importing tours as JSON
  tourLogs?: TourLog[];
}