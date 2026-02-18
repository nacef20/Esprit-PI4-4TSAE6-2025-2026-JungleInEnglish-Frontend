import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../constants/api.constants';
import type { ProductOrder, PlaceOrderRequest, OrderStatusEnum } from '../../shared/models/order.model';
import type { PageResponse } from '../../shared/models/page.model';

const URL = `${API_CONFIG.apiUrl}/orders`;

@Injectable({ providedIn: 'root' })
export class OrderService {
    private readonly http = inject(HttpClient);

    getAll(pageNo = 0, pageSize = 10): Observable<PageResponse<ProductOrder>> {
        const params = new HttpParams().set('pageNo', pageNo).set('pageSize', pageSize);
        return this.http.get<PageResponse<ProductOrder>>(URL, { params });
    }

    getByUser(userId: number): Observable<ProductOrder[]> {
        return this.http.get<ProductOrder[]>(`${URL}/user/${userId}`);
    }

    searchByOrderId(orderId: string): Observable<ProductOrder> {
        return this.http.get<ProductOrder>(`${URL}/search`, { params: new HttpParams().set('orderId', orderId) });
    }

    placeOrder(request: PlaceOrderRequest): Observable<any> {
        return this.http.post(URL, request);
    }

    updateStatus(orderId: number, statusId: number): Observable<ProductOrder> {
        return this.http.put<ProductOrder>(`${URL}/${orderId}/status`, null, { params: new HttpParams().set('st', statusId) });
    }

    getStatuses(): Observable<OrderStatusEnum[]> {
        return this.http.get<OrderStatusEnum[]>(`${URL}/statuses`);
    }

    delete(id: number): Observable<any> {
        return this.http.delete(`${URL}/${id}`);
    }

    getById(id: number): Observable<ProductOrder> {
        return this.http.get<ProductOrder>(`${URL}/${id}`);
    }

    update(id: number, order: any): Observable<ProductOrder> {
        return this.http.put<ProductOrder>(`${URL}/${id}`, order);
    }
}
