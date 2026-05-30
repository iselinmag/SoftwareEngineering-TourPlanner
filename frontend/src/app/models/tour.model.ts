// IF CHANGE MESSAGE FIRST!

export enum TransportType {
  Bike = 'Bike',
  Hike = 'Hike',
  Running = 'Running',
  Vacation = 'Vacation'
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
  popularity?: number;
  childFriendliness?: string;
}
