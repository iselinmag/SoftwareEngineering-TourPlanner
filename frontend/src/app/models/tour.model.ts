// IF CHANGE MESSAGE FIRST!

export enum TransportType {
  Walk = 'Walk',
  Hike = 'Hike',
  Bike = 'Bike',
  Car = 'Car',
  Boat = 'Boat',
  Run = 'Run'
}

export interface Tour {
  id?: number; // optional field
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  transportType: TransportType;
  distance: number; 
  estimatedTime: string; 
  routeInformation?: string; 

  // computed by the backend, read only on the frontend
  popularity?: number;        // score 0–100
  popularityLevel?: string;   // human-readable label
  childFriendliness?: string;
}
