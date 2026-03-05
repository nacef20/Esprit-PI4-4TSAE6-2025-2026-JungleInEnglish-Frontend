import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { Evenement } from '../models/evenement.model';

/** Normalize so imageUrl is always set from backend (handles image_url or imageUrl). */
function normalizeEvenement(e: Evenement & { image_url?: string }): Evenement {
  const url = e?.imageUrl ?? e?.image_url ?? '';
  const imageUrl = typeof url === 'string' && url.trim().length > 0 ? url.trim() : undefined;
  const { image_url: _drop, ...rest } = e;
  return { ...rest, imageUrl };
}

@Injectable({ providedIn: 'root' })
export class EvenementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.eventApiUrl}/api/evenements`;

  /** Cache image by eventId so images show even if backend doesn't persist/return them yet. */
  private imageCache = new Map<number, string>();

  getAll(): Observable<Evenement[]> {
    return this.http
      .get<Evenement[]>(`${this.baseUrl}/find`)
      .pipe(
        map((list) => list.map(normalizeEvenement)),
        map((list) => list.map((e) => this.mergeCachedImage(e)))
      );
  }

  getById(id: number): Observable<Evenement> {
    return this.http
      .get<Evenement>(`${this.baseUrl}/${id}`)
      .pipe(
        map(normalizeEvenement),
        map((e) => this.mergeCachedImage(e))
      );
  }

  create(evenement: Evenement): Observable<Evenement> {
    const imageToCache = evenement.imageUrl?.trim() || (evenement as Evenement & { image_url?: string }).image_url?.trim();
    const body = {
      ...evenement,
      image_url: evenement.imageUrl ?? (evenement as Evenement & { image_url?: string }).image_url,
    };
    return this.http.post<Evenement>(`${this.baseUrl}/add`, body).pipe(
      map(normalizeEvenement),
      map((e) => {
        if (e.eventId != null && imageToCache) this.imageCache.set(e.eventId, imageToCache);
        return this.mergeCachedImage(e);
      })
    );
  }

  update(id: number, evenement: Evenement): Observable<Evenement> {
    const imageToCache = evenement.imageUrl?.trim() || (evenement as Evenement & { image_url?: string }).image_url?.trim();
    const body = {
      ...evenement,
      image_url: evenement.imageUrl ?? (evenement as Evenement & { image_url?: string }).image_url,
    };
    return this.http.put<Evenement>(`${this.baseUrl}/update?id=${id}`, body).pipe(
      map(normalizeEvenement),
      map((e) => {
        if (imageToCache) this.imageCache.set(id, imageToCache);
        return this.mergeCachedImage(e);
      })
    );
  }

  private mergeCachedImage(e: Evenement): Evenement {
    if (e.eventId == null) return e;
    const cached = this.imageCache.get(e.eventId);
    const fromApi = e.imageUrl?.trim();
    if (cached && !fromApi) return { ...e, imageUrl: cached };
    if (fromApi) this.imageCache.set(e.eventId, fromApi);
    return e;
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete?id=${id}`);
  }

  getAvailableSeats(id: number): Observable<{ availableSeats: number }> {
    return this.http.get<{ availableSeats: number }>(
      `${this.baseUrl}/${id}/available-seats`
    );
  }

  isFull(id: number): Observable<{ full: boolean }> {
    return this.http.get<{ full: boolean }>(
      `${this.baseUrl}/${id}/is-full`
    );
  }
}
