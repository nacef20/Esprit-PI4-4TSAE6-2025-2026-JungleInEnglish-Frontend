import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../constants/api.constants';
import type { Cart } from '../../shared/models/cart.model';

const URL = `${API_CONFIG.apiUrl}/cart`;

@Injectable({ providedIn: 'root' })
export class CartService {
    private readonly http = inject(HttpClient);

    getByUser(userId: number): Observable<Cart[]> {
        return this.http.get<Cart[]>(URL, { params: new HttpParams().set('userId', userId) });
    }

    getCount(userId: number): Observable<{ count: number }> {
        return this.http.get<{ count: number }>(`${URL}/count`, { params: new HttpParams().set('userId', userId) });
    }

    add(productId: number, userId: number): Observable<Cart> {
        return this.http.post<Cart>(URL, { productId, userId });
    }

    updateQuantity(cartId: number, action: 'de' | 'in'): Observable<any> {
        return this.http.put(`${URL}/${cartId}/quantity`, null, { params: new HttpParams().set('sy', action) });
    }

    remove(cartId: number): Observable<void> {
        return this.http.delete<void>(`${URL}/${cartId}`);
    }
}
