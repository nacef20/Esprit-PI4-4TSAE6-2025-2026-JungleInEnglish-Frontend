import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Evenement, Participation, Feedback } from '../models/event.model';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private baseUrl = 'http://localhost:8222/api';
  private gatewayUrl = 'http://localhost:8222';

  constructor(private http: HttpClient) {}
  
  getMediaUrl(imageUrl?: string): string {
    if (!imageUrl) return 'assets/images/default-event.jpg';
    if (imageUrl.startsWith('http') || imageUrl.startsWith('data:')) return imageUrl;
    return `${this.gatewayUrl}${imageUrl}`;
  }

  // --- Evenement API ---
  getEvents(): Observable<Evenement[]> {
    return this.http.get<Evenement[]>(`${this.baseUrl}/evenements/find`);
  }

  getEventById(id: number): Observable<Evenement> {
    return this.http.get<Evenement>(`${this.baseUrl}/evenements/${id}`);
  }

  createEvent(event: Evenement): Observable<Evenement> {
    return this.http.post<Evenement>(`${this.baseUrl}/evenements/add`, event);
  }

  updateEvent(id: number, event: Evenement): Observable<Evenement> {
    const params = new HttpParams().set('id', id.toString());
    return this.http.put<Evenement>(`${this.baseUrl}/evenements/update`, event, { params });
  }

  deleteEvent(id: number): Observable<void> {
    const params = new HttpParams().set('id', id.toString());
    return this.http.delete<void>(`${this.baseUrl}/evenements/delete`, { params });
  }

  getAvailableSeats(id: number): Observable<{ availableSeats: number }> {
    return this.http.get<{ availableSeats: number }>(`${this.baseUrl}/evenements/${id}/available-seats`);
  }

  // --- Participation API ---
  register(userId: string, eventId: number): Observable<Participation> {
    return this.http.post<Participation>(`${this.baseUrl}/participations/register`, { userId, eventId });
  }

  getParticipations(): Observable<Participation[]> {
    return this.http.get<Participation[]>(`${this.baseUrl}/participations/findall`);
  }

  scanQrCode(token: string): Observable<Participation> {
    return this.http.post<Participation>(`${this.baseUrl}/participations/scan`, { token });
  }

  checkIn(id: number): Observable<Participation> {
    const params = new HttpParams().set('id', id.toString());
    return this.http.post<Participation>(`${this.baseUrl}/participations/checkin`, null, { params });
  }

  getParticipationByToken(token: string): Observable<Participation> {
    const params = new HttpParams().set('token', token);
    return this.http.get<Participation>(`${this.baseUrl}/participations/by-token`, { params });
  }

  getParticipantsByEvent(eventId: number): Observable<Participation[]> {
    return this.http.get<Participation[]>(`${this.baseUrl}/participations/event/${eventId}`);
  }

  getParticipationsByUser(userId: string): Observable<Participation[]> {
    return this.http.get<Participation[]>(`${this.baseUrl}/participations/user/${userId}`);
  }

  unregister(participationId: number): Observable<void> {
    const params = new HttpParams().set('id', participationId.toString());
    return this.http.delete<void>(`${this.baseUrl}/participations/unregister`, { params });
  }

  // --- Feedback API ---
  addFeedback(feedback: Feedback): Observable<Feedback> {
    return this.http.post<Feedback>(`${this.baseUrl}/event-feedbacks/add`, feedback);
  }

  getFeedbacksByEvent(eventId: number): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.baseUrl}/event-feedbacks/event/${eventId}`);
  }

  deleteFeedback(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/event-feedbacks/delete?id=${id}`);
  }

  // Métier Avancé: Suggestion de créneau
  suggestTimeSlot(fromDate: string, toDate: string, duration: number, level: string): Observable<{ suggestedTime: string }> {
    const params = { fromDate, toDate, durationInHours: duration.toString(), level };
    return this.http.get<{ suggestedTime: string }>(`${this.baseUrl}/evenements/suggest-time-slot`, { params });
  }
}
