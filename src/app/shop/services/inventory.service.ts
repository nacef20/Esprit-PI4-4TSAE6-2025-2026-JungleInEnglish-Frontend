import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

export interface InventoryAlertDTO {
  productId: number;
  productTitle: string;
  currentStock: number;
  minStockLevel: number;
  deficit: number;
}

export interface ProductSalesDTO {
  productId: number;
  productTitle: string;
  totalQuantitySold: number;
  totalRevenue: number;
}

export interface InventoryReportDTO {
  fastMovingProducts: ProductSalesDTO[];
  slowMovingProducts: ProductSalesDTO[];
  outOfStockProducts: InventoryAlertDTO[];
  generatedAt: string;
}

export interface RestockResponseDTO {
  productId: number;
  productTitle: string;
  previousStock: number;
  addedQuantity: number;
  newStock: number;
  recommendedRestockQuantity: number;
  restockedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private apiUrl = `${environment.apiBaseUrl}/api/inventory`;

  constructor(private http: HttpClient) { }

  getAlerts(): Observable<InventoryAlertDTO[]> {
    return this.http.get<InventoryAlertDTO[]>(`${this.apiUrl}/alerts`);
  }

  getTopProducts(limit: number = 10): Observable<ProductSalesDTO[]> {
    return this.http.get<ProductSalesDTO[]>(`${this.apiUrl}/top-products`, {
      params: { limit: limit.toString() }
    });
  }

  getReport(): Observable<InventoryReportDTO> {
    return this.http.get<InventoryReportDTO>(`${this.apiUrl}/report`);
  }

  restockProduct(productId: number, quantity?: number): Observable<RestockResponseDTO> {
    let params: any = {};
    if (quantity !== undefined && quantity !== null) {
      params.quantity = quantity.toString();
    }
    return this.http.post<RestockResponseDTO>(`${this.apiUrl}/restock/${productId}`, {}, { params });
  }
}
