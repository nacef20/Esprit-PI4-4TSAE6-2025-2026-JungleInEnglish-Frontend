import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

export interface PaymentInitiateRequest {
  email?: string;
  amount?: number;
  orderDetailsText?: string;
}

export interface PaymentConfirmRequest {
  sessionId: string;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
}

export interface PaymentResponse {
  sessionId: string;
  status: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = `${environment.apiBaseUrl}/api/payments`;

  initiate(data: PaymentInitiateRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.apiBaseUrl}/initiate`, data);
  }

  confirm(data: PaymentConfirmRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.apiBaseUrl}/confirm`, data);
  }
}
