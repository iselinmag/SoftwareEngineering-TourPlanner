// IF CHANGE MESSAGE FIRST!

export enum Difficulty {
  Easy = 'Easy',
  Medium = 'Medium',
  Hard = 'Hard'
}

export interface TourLog {
  id?: number;
  ownerUsername?: string;   // who wrote this log
  tourId: number; 
  dateTime: Date | string;
  comment: string;
  difficulty: Difficulty;
  totalDistance: number;
  totalTime: string;
  rating: number; 
}