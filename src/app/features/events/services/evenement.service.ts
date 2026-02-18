import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { Evenement } from '../models/evenement.model';

@Injectable({ providedIn: 'root' })
export class EvenementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.eventApiUrl}/api/evenements`;

  getAll(): Observable<Evenement[]> {
    return this.http.get<Evenement[]>(`${this.baseUrl}/find`);
  }

  getById(id: number): Observable<Evenement> {
    return this.http.get<Evenement>(`${this.baseUrl}/${id}`);
  }

  create(evenement: Evenement): Observable<Evenement> {
    return this.http.post<Evenement>(`${this.baseUrl}/add`, evenement);
  }

  update(id: number, evenement: Evenement): Observable<Evenement> {
    return this.http.put<Evenement>(`${this.baseUrl}/update?id=${id}`, evenement);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete?id=${id}`);
  }

  getAvailableSeats(id: number): Observable<{ availableSeats: number }> {
    return this.http.get<{ availableSeats: number }>(
      `${this.baseUrl}/${id}/available-seats`
    );
  }

  isFull(id: number): Observable<{ full: boolean }> {
    return this.http.get<{ full: boolean }>(
      `${this.baseUrl}/${id}/is-full`
    );
  }
}
