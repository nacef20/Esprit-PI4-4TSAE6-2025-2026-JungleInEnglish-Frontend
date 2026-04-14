import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Lesson } from '../models/lesson.model';
import { LessonAttachment, AttachmentType } from '../models/lesson-attachment.model';

export interface CourseProgressSummary {
  courseId: number;
  totalLessons: number;
  completedLessons: number;
  completionPercent: number;
  nextLessonId: number | null;
  unlockedOrderIndexMax: number;
}

export interface LessonDifficultyDTO {
  lessonId: number;
  startedCount: number;
  completedCount: number;
  completionRate: number;
  avgTimeSeconds: number;
  difficulty: 'HARD' | 'MEDIUM' | 'EASY';
}

@Injectable({
  providedIn: 'root'
})
export class LessonsService {
  private apiUrl = `${environment.apiUrl}/api/lessons`;

  constructor(private http: HttpClient) {}

  createLesson(lesson: Lesson): Observable<Lesson> {
    return this.http.post<Lesson>(`${this.apiUrl}/addLesson`, lesson);
  }

  getLesson(id: number): Observable<Lesson> {
    return this.http.get<Lesson>(`${this.apiUrl}/findLesson/${id}`);
  }

  getAllLessons(): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.apiUrl}/findallLessons`);
  }

  updateLesson(id: number, lesson: Lesson): Observable<Lesson> {
    return this.http.put<Lesson>(`${this.apiUrl}/updateLesson/${id}`, lesson);
  }

  deleteLesson(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/deleteLesson/${id}`);
  }

  getLessonsByCourse(courseId: number): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.apiUrl}/course/${courseId}`);
  }

  getLessonAttachments(lessonId: number): Observable<LessonAttachment[]> {
    return this.http.get<LessonAttachment[]>(`${this.apiUrl}/${lessonId}/attachments`);
  }

  uploadLessonAttachment(lessonId: number, type: AttachmentType, file: File): Observable<LessonAttachment> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<LessonAttachment>(`${this.apiUrl}/${lessonId}/attachments?type=${type}`, formData);
  }

  getAttachmentUrl(attachmentId: number): string {
    return `${this.apiUrl}/attachments/${attachmentId}`;
  }
getAttachmentBlob(attachmentId: number): Observable<Blob> {
  return this.http.get(`${this.apiUrl}/attachments/${attachmentId}`, {
    responseType: 'blob'
  });
}
  private getExt(fileName: string): string {
    const i = fileName.lastIndexOf('.');
    return i >= 0 ? fileName.slice(i + 1).toLowerCase() : '';
  }

  private isTypeMatchingFile(type: AttachmentType, file: File): boolean {
    const ext = this.getExt(file.name);
    const ct = (file.type || '').toLowerCase();

    switch (type) {
      case 'PDF':
        return ext === 'pdf' || ct === 'application/pdf';
      case 'WORD':
        return ['doc', 'docx'].includes(ext)
          || ct.includes('msword')
          || ct.includes('officedocument.wordprocessingml');
      case 'PPT':
        return ['ppt', 'pptx'].includes(ext)
          || ct.includes('ms-powerpoint')
          || ct.includes('officedocument.presentationml');
      case 'VIDEO':
        return ['mp4', 'webm', 'mov', 'mkv'].includes(ext) || ct.startsWith('video/');
      case 'OTHER':
      default:
        return true;
    }
  }

  startLessonProgress(lessonId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/progress/lesson/start`, { lessonId });
  }

  completeLessonProgress(lessonId: number, timeSpentSeconds?: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/progress/lesson/complete`, {
      lessonId,
      timeSpentSeconds: timeSpentSeconds ?? null
    });
  }

  completeAttachmentProgress(attachmentId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/progress/attachment/complete`, { attachmentId });
  }

  getCourseProgressSummary(courseId: number): Observable<CourseProgressSummary> {
    return this.http.get<CourseProgressSummary>(`${this.apiUrl}/progress/course/${courseId}/summary`);
  }

  getLessonDifficulty(courseId: number): Observable<LessonDifficultyDTO[]> {
    return this.http.get<LessonDifficultyDTO[]>(
      `${this.apiUrl}/progress/course/${courseId}/difficulty`
    );
  }

  deleteLessonAttachment(attachmentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/attachments/${attachmentId}`);
  }

  enrollCourse(courseId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/progress/course/${courseId}/enroll`, {});
  }

  getMyCourseIds(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/progress/my-courses`);
  }

  getTop3SuggestedCourseIds(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/progress/recommendations/top3`);
  }
}