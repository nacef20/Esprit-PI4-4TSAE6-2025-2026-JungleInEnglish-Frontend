import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Attempt } from '../models/attempt.model';

@Injectable({ providedIn: 'root' })
export class AttemptService {

  private apiUrl = 'http://localhost:8089/api/attempts';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Attempt[]> {
    return this.http.get<Attempt[]>(this.apiUrl);
  }

  getById(id: number): Observable<Attempt> {
    return this.http.get<Attempt>(`${this.apiUrl}/${id}`);
  }

  create(attempt: Attempt): Observable<Attempt> {
    return this.http.post<Attempt>(this.apiUrl, attempt);
  }

  update(id: number, attempt: Attempt): Observable<Attempt> {
    return this.http.put<Attempt>(this.apiUrl, { ...attempt, id });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
