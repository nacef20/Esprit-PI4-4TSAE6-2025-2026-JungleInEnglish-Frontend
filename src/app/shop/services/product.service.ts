import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../constants/api.constants';
import type { Product, ProductCreate, ProductUpdate } from '@shop/shared/models/product.model';
import type { PageResponse } from '@shop/shared/models/page.model';

const URL = `${API_CONFIG.apiUrl}/products`;

@Injectable({ providedIn: 'root' })
export class ProductService {
    private readonly http = inject(HttpClient);

    getAll(pageNo = 0, pageSize = 10, search = ''): Observable<PageResponse<Product>> {
        let params = new HttpParams().set('pageNo', pageNo).set('pageSize', pageSize);
        if (search) params = params.set('search', search);
        return this.http.get<PageResponse<Product>>(URL, { params });
    }

    getActive(pageNo = 0, pageSize = 12, category = '', search = ''): Observable<PageResponse<Product>> {
        let params = new HttpParams().set('pageNo', pageNo).set('pageSize', pageSize);
        if (category) params = params.set('category', category);
        if (search) params = params.set('search', search);
        return this.http.get<PageResponse<Product>>(`${URL}/active`, { params });
    }

    getById(id: number): Observable<Product> {
        return this.http.get<Product>(`${URL}/${id}`);
    }

    create(dto: ProductCreate, image?: File): Observable<Product> {
        const formData = new FormData();
        formData.append('product', new Blob([JSON.stringify(dto)], { type: 'application/json' }));
        if (image) formData.append('imageFile', image);
        return this.http.post<Product>(URL, formData);
    }

    update(id: number, dto: ProductUpdate, image?: File): Observable<Product> {
        const formData = new FormData();
        formData.append('product', new Blob([JSON.stringify(dto)], { type: 'application/json' }));
        if (image) formData.append('imageFile', image);
        return this.http.put<Product>(`${URL}/${id}`, formData);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${URL}/${id}`);
    }

    getImageUrl(imageName: string): string {
        return imageName ? `${environment.apiBaseUrl}/img/product_img/${imageName}` : 'assets/placeholder.png';
    }
}
