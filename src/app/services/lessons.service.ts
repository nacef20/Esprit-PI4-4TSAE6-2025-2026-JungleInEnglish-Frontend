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

  constructor(private http: HttpClient) { }

  // =========================
  // Lessons CRUD (yours)
  // =========================
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

  // =========================
  // Attachments (from earlier)
  // =========================
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
  
  // =========================
  // ✅ Progress (NEW)
  // =========================
getLearnerKey(): string {
  const keyName = 'learnerKey';
  const existing = localStorage.getItem(keyName);
  if (existing) return existing;

  const generated =
    (typeof crypto !== 'undefined' && (crypto as any).randomUUID)
      ? (crypto as any).randomUUID()
      : `${Date.now()}-${Math.random().toString(16).slice(2)}`;

  localStorage.setItem(keyName, generated);
  return generated;
}
  startLessonProgress(lessonId: number): Observable<void> {
    const learnerKey = this.getLearnerKey();
    return this.http.post<void>(`${this.apiUrl}/progress/lesson/start`, { learnerKey, lessonId });
  }

  completeLessonProgress(lessonId: number, timeSpentSeconds?: number): Observable<void> {
    const learnerKey = this.getLearnerKey();
    return this.http.post<void>(`${this.apiUrl}/progress/lesson/complete`, {
      learnerKey,
      lessonId,
      timeSpentSeconds: timeSpentSeconds ?? null
    });
  }

  completeAttachmentProgress(attachmentId: number): Observable<void> {
    const learnerKey = this.getLearnerKey();
    return this.http.post<void>(`${this.apiUrl}/progress/attachment/complete`, { learnerKey, attachmentId });
  }

  getCourseProgressSummary(courseId: number): Observable<CourseProgressSummary> {
    const learnerKey = this.getLearnerKey();
    return this.http.get<CourseProgressSummary>(`${this.apiUrl}/progress/course/${courseId}/summary?learnerKey=${learnerKey}`);
  }

  getLessonDifficulty(courseId: number) {
  return this.http.get<LessonDifficultyDTO[]>(
    `${this.apiUrl}/progress/course/${courseId}/difficulty`
  );
}
  
  deleteLessonAttachment(attachmentId: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/attachments/${attachmentId}`);
}
// ✅ Enrollment
enrollCourse(courseId: number): Observable<void> {
  const learnerKey = this.getLearnerKey();
  return this.http.post<void>(
    `${this.apiUrl}/progress/course/${courseId}/enroll?learnerKey=${learnerKey}`,
    {}
  );
}

// ✅ My courses (ids)
getMyCourseIds(): Observable<number[]> {
  const learnerKey = this.getLearnerKey();
  return this.http.get<number[]>(
    `${this.apiUrl}/progress/my-courses?learnerKey=${learnerKey}`
  );
}

// ✅ Top 3 suggested course IDs
getTop3SuggestedCourseIds(): Observable<number[]> {
  const learnerKey = this.getLearnerKey();
  return this.http.get<number[]>(
    `${this.apiUrl}/progress/recommendations/top3?learnerKey=${learnerKey}`
  );
}
}