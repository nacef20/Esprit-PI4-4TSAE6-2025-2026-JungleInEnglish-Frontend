export interface Lesson {
  id?: number;
  title: string;
  content: string;
  videoUrl?: string;
  duration?: string;
  orderIndex: number;
  courseId: number;
}