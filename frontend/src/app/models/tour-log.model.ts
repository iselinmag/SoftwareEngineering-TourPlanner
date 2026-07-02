// this describes the shape of a tour log on the frontend side.
// it is just a plain description of the fields a log has, so the rest of the app knows
// what to expect. it has to line up with the log shape the backend sends and receives.

// the short fixed menu of how hard a tour felt
export enum Difficulty {
  Easy = 'Easy',
  Medium = 'Medium',
  Hard = 'Hard'
}

export interface TourLog {
  id?: number;
  ownerUsername?: string;   // who wrote this log, so the ui can show edit and delete only to them
  tourId: number;           // which tour this log belongs to
  dateTime: Date | string;
  comment: string;
  difficulty: Difficulty;
  totalDistance: number;
  totalTime: string;
  rating: number;
  imagePath?: string;       // file name of this log's image, or nothing if it has none
}
