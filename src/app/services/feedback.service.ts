import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  FeedbackRequest,
  FeedbackResponse,
  ReactionRequest,
  ProfessorBadgeResponse
} from '../models/feedback.model';

@Injectable({ providedIn: 'root' })
export class FeedbackService {
private readonly base = 'http://localhost:8222/api';

  constructor(private http: HttpClient) {}

  // ── Feedbacks ────────────────────────────────────────────────────────────

  addFeedback(payload: FeedbackRequest): Observable<FeedbackResponse> {
    return this.http.post<FeedbackResponse>(`${this.base}/feedbacks/addFeedback`, payload);
  }

  getFeedbacksByLesson(lessonId: number): Observable<FeedbackResponse[]> {
    return this.http.get<FeedbackResponse[]>(`${this.base}/feedbacks/lesson/${lessonId}`);
  }

  getFeedbacksByCourse(courseId: number): Observable<FeedbackResponse[]> {
    return this.http.get<FeedbackResponse[]>(`${this.base}/feedbacks/course/${courseId}`);
  }

  deleteFeedback(feedbackId: number, studentId: string): Observable<void> {
    const params = new HttpParams().set('studentId', studentId);
    return this.http.delete<void>(`${this.base}/feedbacks/deleteFeedback/${feedbackId}`, { params });
  }

  // ── Reactions ────────────────────────────────────────────────────────────

  addReaction(feedbackId: number, payload: ReactionRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/feedbacks/${feedbackId}/addReaction`, payload);
  }

  updateReaction(feedbackId: number, payload: ReactionRequest): Observable<void> {
    return this.http.put<void>(`${this.base}/feedbacks/${feedbackId}/updateReaction`, payload);
  }

  removeReaction(feedbackId: number, studentId: string): Observable<void> {
    const params = new HttpParams().set('studentId', studentId);
    return this.http.delete<void>(`${this.base}/feedbacks/${feedbackId}/deleteReaction`, { params });
  }

  // ── Professor Badges ─────────────────────────────────────────────────────

  getProfessorBadge(professorId: string): Observable<ProfessorBadgeResponse> {
    return this.http.get<ProfessorBadgeResponse>(`${this.base}/professor-badges/findBadge/${professorId}`);
  }

  recalculateBadge(professorId: string): Observable<ProfessorBadgeResponse> {
    return this.http.post<ProfessorBadgeResponse>(
      `${this.base}/professor-badges/recalculateBadge/${professorId}`, {}
    );
  }
}