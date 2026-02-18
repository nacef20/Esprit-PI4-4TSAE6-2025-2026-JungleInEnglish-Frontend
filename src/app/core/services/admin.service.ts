import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../constants/api.constants';
import type { User } from '../../shared/models/user.model';

const URL = `${API_CONFIG.apiUrl}/admin`;

@Injectable({ providedIn: 'root' })
export class AdminService {
    private readonly http = inject(HttpClient);

    getDashboard(): Observable<{ totalUsers: number; totalProducts: number; totalCategories: number; totalOrders: number }> {
        return this.http.get<any>(`${URL}/dashboard`);
    }

    getUsers(type: 1 | 2 = 1): Observable<User[]> {
        return this.http.get<User[]>(`${URL}/users`, { params: { type } });
    }

    updateAccountStatus(userId: number, status: boolean): Observable<any> {
        return this.http.put(`${URL}/users/${userId}/status`, null, { params: { status } });
    }

    addAdmin(data: { name: string; email: string; password: string }): Observable<User> {
        return this.http.post<User>(`${URL}/add-admin`, data);
    }
}
