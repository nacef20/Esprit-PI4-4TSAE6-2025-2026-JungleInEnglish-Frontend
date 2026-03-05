import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { EvenementService } from '../../services/evenement.service';
import { getEventImageUrl, type Evenement } from '../../models/evenement.model';
import { SafeUrlPipe } from '../../pipes/safe-url.pipe';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, SafeUrlPipe],
  template: `
    <div class="event-detail-page">
      @if (event(); as ev) {
        <a routerLink="/events" class="back-link">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to Events
        </a>
        <div class="event-hero-section">
          @if (getEventImageUrl(ev); as imgUrl) {
            @if (!heroImgFailed) {
              <div class="event-hero-image">
                <img [src]="imgUrl | safeUrl" [alt]="ev.title" (error)="heroImgFailed = true" />
              </div>
            } @else {
              <div class="event-hero-placeholder">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                <p>Image unavailable</p>
              </div>
            }
          } @else {
            <div class="event-hero-placeholder">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <p>No image available</p>
            </div>
          }
        </div>
        <div class="event-content-wrapper">
          <div class="event-main">
            <h1 class="event-title">{{ ev.title }}</h1>
            @if (ev.description) {
              <p class="event-description">{{ ev.description }}</p>
            }
          </div>
          <div class="event-details-card">
            <h2 class="details-title">Event Details</h2>
            <div class="details-grid">
              @if (ev.location) {
                <div class="detail-item">
                  <div class="detail-icon location">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                  <div class="detail-content">
                    <span class="detail-label">Location</span>
                    <span class="detail-value">{{ ev.location }}</span>
                  </div>
                </div>
              }
              <div class="detail-item">
                <div class="detail-icon calendar">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div class="detail-content">
                  <span class="detail-label">Start Time</span>
                  <span class="detail-value">{{ ev.startTime | date:'medium' }}</span>
                </div>
              </div>
              <div class="detail-item">
                <div class="detail-icon calendar">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div class="detail-content">
                  <span class="detail-label">End Time</span>
                  <span class="detail-value">{{ ev.endTime | date:'medium' }}</span>
                </div>
              </div>
              <div class="detail-item">
                <div class="detail-icon users">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a4 4 0 00-4-4h-1m-4 6H2v-2a4 4 0 014-4h1m6 6v-2a4 4 0 00-4-4H8m8-4a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
                <div class="detail-content">
                  <span class="detail-label">Capacity</span>
                  <span class="detail-value">{{ ev.capacity }} people</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      } @else if (loading()) {
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Loading event...</p>
        </div>
      } @else {
        <div class="error-state">
          <h2>Event not found</h2>
          <p>The event you're looking for doesn't exist or has been removed.</p>
          <a routerLink="/events" class="back-link">← Back to Events</a>
        </div>
      }
    </div>
  `,
  styles: `
    .event-detail-page {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }
    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      color: #0ea37a;
      text-decoration: none;
      font-weight: 600;
      margin-bottom: 1.5rem;
      transition: color 0.2s;
    }
    .back-link:hover {
      color: #0b8a66;
    }
    .back-link svg {
      width: 20px;
      height: 20px;
    }
    .event-hero-section {
      margin-bottom: 2rem;
      border-radius: 20px;
      overflow: hidden;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }
    .event-hero-image {
      width: 100%;
      height: 500px;
      overflow: hidden;
      background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
    }
    .event-hero-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
    .event-hero-placeholder {
      width: 100%;
      height: 400px;
      background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #94a3b8;
    }
    .event-hero-placeholder svg {
      width: 80px;
      height: 80px;
      opacity: 0.5;
      margin-bottom: 1rem;
    }
    .event-hero-placeholder p {
      margin: 0;
      font-size: 1.1rem;
    }
    .event-content-wrapper {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 2rem;
    }
    .event-main {
      background: white;
      padding: 2rem;
      border-radius: 16px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      border: 1px solid #e2e8f0;
    }
    .event-title {
      font-size: 2.5rem;
      font-weight: 700;
      color: #0ea37a;
      margin: 0 0 1rem 0;
      line-height: 1.2;
    }
    .event-description {
      font-size: 1.1rem;
      color: #64748b;
      line-height: 1.7;
      margin: 0;
    }
    .event-details-card {
      background: white;
      padding: 2rem;
      border-radius: 16px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      border: 1px solid #e2e8f0;
      height: fit-content;
    }
    .details-title {
      font-size: 1.5rem;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 1.5rem 0;
    }
    .details-grid {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .detail-item {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
    }
    .detail-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .detail-icon svg {
      width: 24px;
      height: 24px;
    }
    .detail-icon.location {
      background: rgba(14, 163, 122, 0.1);
      color: #0ea37a;
    }
    .detail-icon.calendar {
      background: rgba(59, 130, 246, 0.1);
      color: #3b82f6;
    }
    .detail-icon.users {
      background: rgba(168, 85, 247, 0.1);
      color: #a855f7;
    }
    .detail-content {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      flex: 1;
    }
    .detail-label {
      font-size: 0.875rem;
      color: #64748b;
      font-weight: 500;
    }
    .detail-value {
      font-size: 1rem;
      color: #1e293b;
      font-weight: 600;
    }
    .loading-state, .error-state {
      text-align: center;
      padding: 4rem 2rem;
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
    .error-state h2 {
      color: #1e293b;
      margin-bottom: 0.5rem;
    }
    .error-state p {
      color: #64748b;
      margin-bottom: 1.5rem;
    }
    @media (max-width: 968px) {
      .event-content-wrapper {
        grid-template-columns: 1fr;
      }
      .event-hero-image {
        height: 350px;
      }
      .event-title {
        font-size: 2rem;
      }
    }
  `,
})
export class EventDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly evenementService = inject(EvenementService);
  protected readonly event = signal<Evenement | null>(null);
  protected readonly loading = signal(true);
  protected heroImgFailed = false;
  protected readonly getEventImageUrl = getEventImageUrl;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.evenementService.getById(id).subscribe({
      next: (e) => {
        this.event.set(e);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
