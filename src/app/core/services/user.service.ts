import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../constants/api.constants';
import type { User, UserCreateDto, UserUpdateDto } from '../../shared/models/user.model';

const USERS_URL = `${API_CONFIG.apiUrl}/users`;

/**
 * Service d'accès à l'API REST Users du backend Spring Boot.
 * GET /api/users, POST /api/users, PUT /api/users/{id}, DELETE /api/users/{id}
 */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(USERS_URL);
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${USERS_URL}/${id}`);
  }

  create(dto: UserCreateDto): Observable<User> {
    return this.http.post<User>(USERS_URL, dto);
  }

  update(id: number, dto: UserUpdateDto): Observable<User> {
    return this.http.put<User>(`${USERS_URL}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${USERS_URL}/${id}`);
  }
}
