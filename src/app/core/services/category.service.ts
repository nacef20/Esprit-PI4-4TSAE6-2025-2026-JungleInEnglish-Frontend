import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../constants/api.constants';
import type { Category, CategoryCreate, CategoryUpdate } from '../../shared/models/category.model';
import type { PageResponse } from '../../shared/models/page.model';

const URL = `${API_CONFIG.apiUrl}/categories`;

@Injectable({ providedIn: 'root' })
export class CategoryService {
    private readonly http = inject(HttpClient);

    getAll(pageNo = 0, pageSize = 10): Observable<PageResponse<Category>> {
        const params = new HttpParams().set('pageNo', pageNo).set('pageSize', pageSize);
        return this.http.get<PageResponse<Category>>(URL, { params });
    }

    getActive(): Observable<Category[]> {
        return this.http.get<Category[]>(`${URL}/active`);
    }

    getById(id: number): Observable<Category> {
        return this.http.get<Category>(`${URL}/${id}`);
    }

    create(dto: CategoryCreate): Observable<Category> {
        return this.http.post<Category>(URL, dto);
    }

    update(id: number, dto: CategoryUpdate): Observable<Category> {
        return this.http.put<Category>(`${URL}/${id}`, dto);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${URL}/${id}`);
    }
}
