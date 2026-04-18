import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import type { Cart } from '@shop/shared/models/cart.model';

@Injectable({ providedIn: 'root' })
export class CartService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/api/cart`;

    getMyCart(): Observable<Cart[]> {
        return this.http.get<Cart[]>(this.apiUrl);
    }

    getCount(): Observable<{ count: number }> {
        return this.http.get<{ count: number }>(`${this.apiUrl}/count`);
    }

    add(productId: number): Observable<Cart> {
        return this.http.post<Cart>(this.apiUrl, { productId });
    }

    updateQuantity(cartId: number, action: 'de' | 'in'): Observable<any> {
        return this.http.put(`${this.apiUrl}/${cartId}/quantity`, null, {
            params: new HttpParams().set('sy', action)
        });
    }

    remove(cartId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${cartId}`);
    }
}
