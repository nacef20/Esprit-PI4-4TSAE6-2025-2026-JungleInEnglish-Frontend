import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LessonHeartbeatRequest {
  courseId: number;
  deltaSeconds: number;
}

@Injectable({ providedIn: 'root' })
export class ProgressService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  heartbeatLesson(lessonId: number, body: LessonHeartbeatRequest): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/lessons/progress/lessons/${lessonId}/heartbeat`,
      body
    );
  }
}