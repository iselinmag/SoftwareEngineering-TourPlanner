// IF CHANGE MESSAGE FIRST!

export enum Difficulty {
  Easy = 'Easy',
  Medium = 'Medium',
  Hard = 'Hard'
}

export interface TourLog {
  id?: string;
  tourId: string; 
  dateTime: Date | string;
  comment: string;
  difficulty: Difficulty;
  totalDistance: number;
  totalTime: string;
  rating: number; 
}