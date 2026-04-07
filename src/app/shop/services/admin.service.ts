import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/api/admin`;

    getDashboard(): Observable<{ totalProducts: number; totalCategories: number; totalOrders: number }> {
        return this.http.get<any>(`${this.apiUrl}/dashboard`);
    }
}
