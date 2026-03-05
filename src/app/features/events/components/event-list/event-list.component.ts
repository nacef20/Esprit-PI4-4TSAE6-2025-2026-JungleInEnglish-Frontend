import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AsyncPipe, DatePipe } from '@angular/common';
import { EvenementService } from '../../services/evenement.service';
import { getEventImageUrl } from '../../models/evenement.model';
import { SafeUrlPipe } from '../../pipes/safe-url.pipe';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [RouterLink, AsyncPipe, DatePipe, SafeUrlPipe],
  template: `
    <div class="events-page">
      <header class="events-header">
        <h1>Events</h1>
        <p class="subtitle">Discover upcoming events and join the community</p>
      </header>
      @if (events$ | async; as events) {
        @if (events.length === 0) {
          <div class="empty-state">
            <div class="empty-icon">📅</div>
            <p>No events yet. Check back soon!</p>
          </div>
        } @else {
          <div class="events-grid">
            @for (event of events; track event.eventId) {
              <a [routerLink]="['/events', event.eventId]" class="event-card">
                <div class="event-image-wrapper">
                  @if (getImgUrl(event); as imgUrl) {
                    <img [src]="imgUrl | safeUrl" [alt]="event.title" class="event-image" (error)="onImgError($event)" />
                  } @else {
                    <div class="event-image-placeholder">
                      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                  }
                  <div class="event-overlay"></div>
                </div>
                <div class="event-content">
                  <h3 class="event-title">{{ event.title }}</h3>
                  @if (event.description) {
                    <p class="event-description">{{ event.description }}</p>
                  }
                  <div class="event-meta">
                    @if (event.location) {
                      <span class="meta-item">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                        {{ event.location }}
                      </span>
                    }
                    <span class="meta-item">
                      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      {{ event.startTime | date:'short' }}
                    </span>
                  </div>
                </div>
              </a>
            }
          </div>
        }
      } @else {
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Loading events...</p>
        </div>
      }
    </div>
  `,
  styles: `
    .events-page {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }
    .events-header {
      margin-bottom: 2.5rem;
      text-align: center;
    }
    .events-header h1 {
      font-size: 2.5rem;
      font-weight: 700;
      color: #0ea37a;
      margin: 0 0 0.5rem 0;
      letter-spacing: -0.02em;
    }
    .subtitle {
      color: #64748b;
      font-size: 1.1rem;
      margin: 0;
    }
    .events-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 2rem;
    }
    .event-card {
      background: white;
      border-radius: 16px;
      overflow: hidden;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      transition: all 0.3s ease;
      text-decoration: none;
      color: inherit;
      display: flex;
      flex-direction: column;
      border: 1px solid #e2e8f0;
    }
    .event-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(14, 163, 122, 0.15);
      border-color: #0ea37a;
    }
    .event-image-wrapper {
      position: relative;
      width: 100%;
      height: 220px;
      overflow: hidden;
      background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
    }
    .event-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    .event-card:hover .event-image {
      transform: scale(1.05);
    }
    .event-image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
      color: #94a3b8;
    }
    .event-image-placeholder svg {
      width: 64px;
      height: 64px;
      opacity: 0.5;
    }
    .event-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 60px;
      background: linear-gradient(to top, rgba(0,0,0,0.3), transparent);
      pointer-events: none;
    }
    .event-content {
      padding: 1.5rem;
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .event-title {
      font-size: 1.25rem;
      font-weight: 700;
      color: #1e293b;
      margin: 0;
      line-height: 1.3;
    }
    .event-description {
      color: #64748b;
      font-size: 0.95rem;
      line-height: 1.5;
      margin: 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .event-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
      margin-top: auto;
      padding-top: 1rem;
      border-top: 1px solid #f1f5f9;
    }
    .meta-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #64748b;
      font-size: 0.875rem;
    }
    .meta-item svg {
      width: 16px;
      height: 16px;
      color: #0ea37a;
    }
    .empty-state, .loading-state {
      text-align: center;
      padding: 4rem 2rem;
    }
    .empty-icon {
      font-size: 4rem;
      margin-bottom: 1rem;
    }
    .empty-state p, .loading-state p {
      color: #64748b;
      font-size: 1.1rem;
    }
    .spinner {
      width: 48px;
      height: 48px;
      border: 4px solid #f1f5f9;
      border-top-color: #0ea37a;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto 1rem;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    @media (max-width: 768px) {
      .events-grid {
        grid-template-columns: 1fr;
        gap: 1.5rem;
      }
      .events-header h1 {
        font-size: 2rem;
      }
    }
  `,
})
export class EventListComponent implements OnInit {
  private readonly evenementService = inject(EvenementService);
  protected readonly events$ = this.evenementService.getAll();
  protected readonly getImgUrl = getEventImageUrl;

  ngOnInit(): void {}

  protected onImgError(e: Event): void {
    const el = e.target as HTMLImageElement;
    const wrap = el.parentElement;
    if (wrap) {
      const placeholder = document.createElement('span');
      placeholder.className = 'event-pic-placeholder';
      placeholder.textContent = 'No image';
      el.replaceWith(placeholder);
    }
  }
}
