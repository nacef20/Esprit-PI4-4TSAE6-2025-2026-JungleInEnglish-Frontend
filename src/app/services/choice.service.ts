import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Choice } from '../models/choice.model';

@Injectable({ providedIn: 'root' })
export class ChoiceService {

  private apiUrl = 'http://localhost:8222/api/choices';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Choice[]> {
    return this.http.get<Choice[]>(this.apiUrl);
  }

  getByQuestionId(questionId: number): Observable<Choice[]> {
    return this.http.get<Choice[]>(`${this.apiUrl}/question/${questionId}`);
  }

  getById(id: number): Observable<Choice> {
    return this.http.get<Choice>(`${this.apiUrl}/${id}`);
  }

  create(choice: Choice): Observable<Choice> {
    return this.http.post<Choice>(this.apiUrl, choice);
  }

  update(id: number, choice: Choice): Observable<Choice> {
    return this.http.put<Choice>(this.apiUrl, { ...choice, id });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
