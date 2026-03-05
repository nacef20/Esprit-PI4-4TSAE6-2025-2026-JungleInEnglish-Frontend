import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AsyncPipe, DatePipe } from '@angular/common';
import { BehaviorSubject, switchMap } from 'rxjs';
import { EvenementService } from '../../events/services/evenement.service';
import type { Evenement } from '../../events/models/evenement.model';
import { getEventImageUrl } from '../../events/models/evenement.model';
import { SafeUrlPipe } from '../../events/pipes/safe-url.pipe';
import { environment } from '../../../../environments/environment';

/** Normalize ISO date to datetime-local input format (YYYY-MM-DDTHH:mm) */
function toDatetimeLocal(iso?: string): string {
  if (!iso) return '';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const h = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${day}T${h}:${min}`;
}

@Component({
  selector: 'app-events-admin',
  standalone: true,
  imports: [FormsModule, AsyncPipe, DatePipe, SafeUrlPipe],
  template: `
    <div class="page">
      <header class="page-header">
        <h1>Events backoffice</h1>
        <p class="subtitle">Create, edit and delete events.</p>
      </header>

      <section class="card card-form">
        <h2>{{ editingEvent ? 'Edit event' : 'Add event' }}</h2>
        <form (ngSubmit)="saveEvent()" class="form">
          <div class="form-row">
            <label for="title">Title</label>
            <input id="title" [(ngModel)]="form.title" name="title" placeholder="Event title" required />
          </div>
          <div class="form-row">
            <label for="description">Description</label>
            <textarea id="description" [(ngModel)]="form.description" name="description" placeholder="Description" rows="2"></textarea>
          </div>
          <div class="form-row">
            <label>Image</label>
            <div class="image-upload-wrap">
              <input
                type="file"
                id="imageFile"
                accept="image/*"
                (change)="onImageSelected($event)"
                class="image-input"
              />
              @if (getFormImageUrl(); as previewUrl) {
                <div class="image-preview">
                  <img [src]="previewUrl | safeUrl" alt="Event" (error)="onPreviewImgError($event)" />
                  <button type="button" class="btn btn-sm btn-danger remove-img" (click)="clearImage()">Remove</button>
                </div>
              } @else {
                <label for="imageFile" class="image-label">Choose image...</label>
              }
            </div>
          </div>
          <div class="form-row two-cols">
            <div>
              <label for="location">Location</label>
              <input id="location" [(ngModel)]="form.location" name="location" placeholder="Location" />
            </div>
            <div>
              <label for="capacity">Capacity</label>
              <input id="capacity" [(ngModel)]="form.capacity" name="capacity" type="number" min="0" placeholder="0" />
            </div>
          </div>
          <div class="form-row two-cols">
            <div>
              <label for="startTime">Start</label>
              <input id="startTime" [(ngModel)]="form.startTime" name="startTime" type="datetime-local" />
            </div>
            <div>
              <label for="endTime">End</label>
              <input id="endTime" [(ngModel)]="form.endTime" name="endTime" type="datetime-local" />
            </div>
          </div>
          @if (!editingEvent) {
            <div class="form-row">
              <label for="organizerId">Organizer ID</label>
              <input id="organizerId" [(ngModel)]="form.organizerId" name="organizerId" type="number" min="1" placeholder="Organizer ID" />
            </div>
          }
          <div class="form-actions">
            @if (editingEvent) {
              <button type="button" class="btn btn-secondary" (click)="cancelEdit()">Cancel</button>
            }
            <button type="submit" class="btn btn-primary">
              {{ editingEvent ? 'Update event' : 'Create event' }}
            </button>
          </div>
        </form>
      </section>

      <section class="card card-table">
        <h2>All events</h2>
        @if (events$ | async; as events) {
          @if (events.length === 0) {
            <p class="empty">No events yet. Create one above.</p>
          } @else {
            <div class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th class="col-pic">Pic</th>
                    <th>Title</th>
                    <th>Location</th>
                    <th>Capacity</th>
                    <th>Start</th>
                    <th>End</th>
                    <th class="col-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (event of events; track event.eventId) {
                    <tr>
                      <td class="col-pic">
                        @if (getEventImageUrl(event); as imgUrl) {
                          <img [src]="imgUrl | safeUrl" [alt]="event.title" class="table-thumb" (error)="onTableImgError($event)" />
                        } @else {
                          <span class="no-pic">—</span>
                        }
                      </td>
                      <td><strong>{{ event.title }}</strong></td>
                      <td>{{ event.location || '—' }}</td>
                      <td>{{ event.capacity }}</td>
                      <td>{{ event.startTime | date:'short' }}</td>
                      <td>{{ event.endTime | date:'short' }}</td>
                      <td class="col-actions">
                        <button type="button" class="btn btn-sm btn-edit" (click)="startEdit(event)">Edit</button>
                        <button type="button" class="btn btn-sm btn-danger" (click)="deleteEvent(event.eventId!)">Delete</button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        }
      </section>
    </div>
  `,
  styles: `
    .page {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem 1rem;
      font-family: 'Rubik', system-ui, -apple-system, sans-serif;
      color: #1e293b;
      background: #f8fafc;
      min-height: 100vh;
    }

    .page-header {
      margin-bottom: 2.5rem;
      text-align: center;
    }

    .page-header h1 {
      margin: 0 0 0.5rem 0;
      font-size: 2.5rem;
      font-weight: 700;
      color: #0ea37a;
      letter-spacing: -0.02em;
    }

    .subtitle {
      margin: 0;
      color: #64748b;
      font-size: 1.1rem;
    }

    .card {
      background: #fff;
      border-radius: 16px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      border: 1px solid #e2e8f0;
      padding: 2rem;
      margin-bottom: 2rem;
    }

    .card h2 {
      margin: 0 0 1.5rem 0;
      font-size: 1.5rem;
      font-weight: 700;
      color: #0ea37a;
      border-bottom: 2px solid #0ea37a;
      padding-bottom: 0.5rem;
    }

    .form .form-row {
      margin-bottom: 1.5rem;
    }

    .form .form-row label {
      display: block;
      font-size: 0.9rem;
      font-weight: 600;
      color: #334155;
      margin-bottom: 0.5rem;
    }

    .form input,
    .form textarea {
      width: 100%;
      padding: 0.75rem 1rem;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      font-size: 0.95rem;
      box-sizing: border-box;
      transition: all 0.2s;
      font-family: inherit;
    }

    .form input:focus,
    .form textarea:focus {
      outline: none;
      border-color: #0ea37a;
      box-shadow: 0 0 0 3px rgba(14, 163, 122, 0.1);
    }

    .form-row.two-cols {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid #e2e8f0;
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border-radius: 10px;
      font-size: 0.95rem;
      font-weight: 600;
      cursor: pointer;
      border: none;
      transition: all 0.2s;
      font-family: inherit;
    }

    .btn-primary {
      background: #0ea37a;
      color: #fff;
    }

    .btn-primary:hover {
      background: #0b8a66;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(14, 163, 122, 0.3);
    }

    .btn-secondary {
      background: #f1f5f9;
      color: #475569;
    }

    .btn-secondary:hover {
      background: #e2e8f0;
    }

    .btn-edit {
      background: rgba(14, 163, 122, 0.1);
      color: #0ea37a;
    }

    .btn-edit:hover {
      background: rgba(14, 163, 122, 0.2);
    }

    .btn-danger {
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
    }

    .btn-danger:hover {
      background: rgba(239, 68, 68, 0.2);
    }

    .btn-sm {
      padding: 0.5rem 1rem;
      font-size: 0.85rem;
      margin-right: 0.5rem;
    }

    .table-wrap {
      overflow-x: auto;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.95rem;
      background: white;
    }

    .data-table th,
    .data-table td {
      padding: 1rem;
      text-align: left;
      border-bottom: 1px solid #f1f5f9;
    }

    .data-table th {
      font-weight: 700;
      color: #0ea37a;
      background: #f8fafc;
      text-transform: uppercase;
      font-size: 0.8rem;
      letter-spacing: 0.5px;
    }

    .data-table tbody tr {
      transition: background 0.2s;
    }

    .data-table tbody tr:hover {
      background: rgba(14, 163, 122, 0.05);
    }

    .col-actions {
      white-space: nowrap;
    }

    .col-pic {
      width: 100px;
      vertical-align: middle;
    }

    .table-thumb {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: 12px;
      display: block;
      border: 2px solid #e2e8f0;
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .table-thumb:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba(14, 163, 122, 0.2);
    }

    .no-pic {
      color: #94a3b8;
      font-size: 0.875rem;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 80px;
      height: 80px;
      background: #f1f5f9;
      border-radius: 12px;
      border: 2px dashed #cbd5e1;
    }

    .image-upload-wrap {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .image-input {
      font-size: 0.9rem;
      padding: 0.5rem;
    }

    .image-label {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 1rem 1.5rem;
      background: linear-gradient(135deg, rgba(14, 163, 122, 0.1) 0%, rgba(14, 163, 122, 0.05) 100%);
      border: 2px dashed #0ea37a;
      border-radius: 12px;
      cursor: pointer;
      color: #0ea37a;
      font-weight: 600;
      transition: all 0.2s;
      margin-bottom: 0 !important;
    }

    .image-label:hover {
      background: rgba(14, 163, 122, 0.15);
      border-color: #0b8a66;
      transform: translateY(-1px);
    }

    .image-preview {
      position: relative;
      display: inline-block;
      border-radius: 12px;
      overflow: hidden;
      border: 2px solid #e2e8f0;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }

    .image-preview img {
      max-width: 300px;
      max-height: 200px;
      object-fit: cover;
      display: block;
    }

    .image-preview .remove-img {
      display: block;
      margin-top: 0.75rem;
    }

    .empty {
      margin: 0;
      color: #64748b;
      font-size: 1rem;
      text-align: center;
      padding: 2rem;
    }

    @media (max-width: 768px) {
      .form-row.two-cols {
        grid-template-columns: 1fr;
      }
      .page-header h1 {
        font-size: 2rem;
      }
      .table-thumb {
        width: 60px;
        height: 60px;
      }
      .no-pic {
        width: 60px;
        height: 60px;
      }
    }
  `,
})
export class EventsAdminComponent implements OnInit {
  private readonly evenementService = inject(EvenementService);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  protected readonly events$ = this.refresh$.pipe(
    switchMap(() => this.evenementService.getAll())
  );

  protected form: Partial<Evenement> & { startTime: string; endTime: string } = this.getEmptyForm();
  protected editingEvent: Evenement | null = null;
  protected readonly getEventImageUrl = getEventImageUrl;

  protected getFormImageUrl(): string | null {
    if (!this.form.imageUrl) return null;
    const url = this.form.imageUrl.trim();
    if (!url) return null;
    // If it's already a full URL or data URL, return as-is
    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
      return url;
    }
    // If it's a relative path, prepend backend URL
    if (url.startsWith('/')) {
      return `${environment.eventApiUrl}${url}`;
    }
    return url;
  }

  ngOnInit(): void {}

  private getEmptyForm(): Partial<Evenement> & { startTime: string; endTime: string } {
    return {
      title: '',
      description: '',
      imageUrl: '',
      location: '',
      capacity: 0,
      startTime: '',
      endTime: '',
      organizerId: 0,
    };
  }

  protected onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input?.files?.[0];
    if (!file || !file.type.startsWith('image/')) return;
    const reader = new FileReader();
    reader.onload = () => {
      this.form = { ...this.form, imageUrl: reader.result as string };
    };
    reader.readAsDataURL(file);
    input.value = '';
  }

  protected clearImage(): void {
    this.form = { ...this.form, imageUrl: '' };
  }

  protected onTableImgError(e: Event): void {
    const img = e.target as HTMLImageElement;
    const td = img.parentElement;
    if (td) {
      const placeholder = document.createElement('span');
      placeholder.className = 'no-pic';
      placeholder.textContent = '—';
      img.replaceWith(placeholder);
    }
  }

  protected onPreviewImgError(e: Event): void {
    const img = e.target as HTMLImageElement;
    img.style.display = 'none';
    const preview = img.parentElement;
    if (preview) {
      const errorMsg = document.createElement('div');
      errorMsg.className = 'image-error';
      errorMsg.textContent = 'Failed to load image';
      errorMsg.style.cssText = 'color: #ef4444; padding: 0.5rem; text-align: center; font-size: 0.875rem;';
      preview.appendChild(errorMsg);
    }
  }

  protected saveEvent(): void {
    if (!this.form.title) return;
    if (this.editingEvent?.eventId != null) {
      const payload: Evenement = {
        eventId: this.editingEvent.eventId,
        title: this.form.title,
        description: this.form.description,
        imageUrl: this.form.imageUrl || undefined,
        location: this.form.location,
        capacity: Number(this.form.capacity) ?? 0,
        startTime: this.form.startTime || this.editingEvent.startTime,
        endTime: this.form.endTime || this.editingEvent.endTime,
        organizerId: this.editingEvent.organizerId,
      };
      this.evenementService.update(this.editingEvent.eventId, payload).subscribe({
        next: () => {
          this.editingEvent = null;
          this.form = this.getEmptyForm();
          this.refresh$.next();
        },
      });
    } else {
      if (!this.form.organizerId) return;
      this.evenementService.create(this.form as Evenement).subscribe({
        next: () => {
          this.form = this.getEmptyForm();
          this.refresh$.next();
        },
      });
    }
  }

  protected startEdit(event: Evenement): void {
    this.editingEvent = event;
    // Store the original imageUrl (relative path) so we can save it back correctly
    const originalImageUrl = event.imageUrl ?? event.image_url ?? '';
    this.form = {
      title: event.title,
      description: event.description ?? '',
      imageUrl: originalImageUrl,
      location: event.location ?? '',
      capacity: event.capacity ?? 0,
      startTime: toDatetimeLocal(event.startTime),
      endTime: toDatetimeLocal(event.endTime),
      organizerId: event.organizerId,
    };
  }

  protected cancelEdit(): void {
    this.editingEvent = null;
    this.form = this.getEmptyForm();
  }

  protected deleteEvent(id: number): void {
    if (confirm('Delete this event? This cannot be undone.')) {
      this.evenementService.delete(id).subscribe({
        next: () => {
          if (this.editingEvent?.eventId === id) {
            this.cancelEdit();
          }
          this.refresh$.next();
        },
      });
    }
  }
}
