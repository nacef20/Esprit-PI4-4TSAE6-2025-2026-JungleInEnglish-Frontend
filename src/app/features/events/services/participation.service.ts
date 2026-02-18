import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { Participation } from '../models/participation.model';

@Injectable({ providedIn: 'root' })
export class ParticipationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.eventApiUrl}/api/participations`;

  getAll(): Observable<Participation[]> {
    return this.http.get<Participation[]>(`${this.baseUrl}/findall`);
  }

  getById(id: number): Observable<Participation> {
    return this.http.get<Participation>(`${this.baseUrl}/find?id=${id}`);
  }

  create(participation: Partial<Participation>): Observable<Participation> {
    return this.http.post<Participation>(`${this.baseUrl}/add`, participation);
  }

  update(
    id: number,
    participation: Partial<Participation>
  ): Observable<Participation> {
    return this.http.put<Participation>(
      `${this.baseUrl}/update?id=${id}`,
      participation
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete?id=${id}`);
  }

  register(userId: number, eventId: number): Observable<Participation> {
    return this.http.post<Participation>(`${this.baseUrl}/register`, {
      userId,
      eventId,
    });
  }

  unregister(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/unregister?id=${id}`);
  }

  checkIn(id: number): Observable<Participation> {
    return this.http.post<Participation>(
      `${this.baseUrl}/checkin?id=${id}`,
      {}
    );
  }
}
