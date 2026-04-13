export enum CourseLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

export interface Course {
  id?: number;
  name: string;
  description: string;
  duration: string;
  level: CourseLevel;
  imageUrl?: string;
  professorId?: string;
  date?: string;
}
