import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import type { ProductOrder, PlaceOrderRequest, OrderStatusEnum } from '@shop/shared/models/order.model';
import type { PageResponse } from '@shop/shared/models/page.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/api/orders`;

    // Admin: Get all orders
    getAll(pageNo = 0, pageSize = 10): Observable<PageResponse<ProductOrder>> {
        const params = new HttpParams().set('pageNo', pageNo).set('pageSize', pageSize);
        return this.http.get<PageResponse<ProductOrder>>(this.apiUrl, { params });
    }

    // Current User: Get my orders automatically using JWT
    getMyOrders(): Observable<ProductOrder[]> {
        return this.http.get<ProductOrder[]>(`${this.apiUrl}/my-orders`);
    }

    // Admin: Get by specific user ID
    getByUser(userId: string): Observable<ProductOrder[]> {
        return this.http.get<ProductOrder[]>(`${this.apiUrl}/user/${userId}`);
    }

    searchByOrderId(orderId: string): Observable<ProductOrder> {
        return this.http.get<ProductOrder>(`${this.apiUrl}/search`, {
            params: new HttpParams().set('orderId', orderId)
        });
    }

    placeOrder(request: PlaceOrderRequest): Observable<any> {
        return this.http.post(this.apiUrl, request);
    }

    updateStatus(orderId: number, statusId: number): Observable<ProductOrder> {
        return this.http.put<ProductOrder>(`${this.apiUrl}/${orderId}/status`, null, {
            params: new HttpParams().set('st', statusId)
        });
    }

    getStatuses(): Observable<OrderStatusEnum[]> {
        return this.http.get<OrderStatusEnum[]>(`${this.apiUrl}/statuses`);
    }

    delete(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/${id}`);
    }

    getById(id: number): Observable<ProductOrder> {
        return this.http.get<ProductOrder>(`${this.apiUrl}/${id}`);
    }

    update(id: number, order: any): Observable<ProductOrder> {
        return this.http.put<ProductOrder>(`${this.apiUrl}/${id}`, order);
    }
}
