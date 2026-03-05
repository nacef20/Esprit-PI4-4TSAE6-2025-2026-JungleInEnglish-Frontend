import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from './auth.service';

@Injectable({ providedIn: 'root' })
export class UserService {
    private readonly http = inject(HttpClient);
    private readonly baseUrl = `${environment.todoApiUrl}/api/users`;

    getStudents(): Observable<User[]> {
        return this.http.get<User[]>(`${this.baseUrl}/students`);
    }

    setStudentLevel(userId: number, level: string): Observable<void> {
        return this.http.put<void>(`${this.baseUrl}/${userId}/level`, { level });
    }
}
