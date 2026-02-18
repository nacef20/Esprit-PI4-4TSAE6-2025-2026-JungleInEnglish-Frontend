import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AsyncPipe, DatePipe } from '@angular/common';
import { BehaviorSubject, switchMap } from 'rxjs';
import { EvenementService } from '../../events/services/evenement.service';
import type { Evenement } from '../../events/models/evenement.model';

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
  imports: [FormsModule, AsyncPipe, DatePipe],
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
      max-width: 960px;
      margin: 0 auto;
      padding: 2rem 1rem;
      font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
      color: #1a1a2e;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .page-header h1 {
      margin: 0 0 0.25rem 0;
      font-size: 1.75rem;
      font-weight: 700;
      letter-spacing: -0.02em;
    }

    .subtitle {
      margin: 0;
      color: #64748b;
      font-size: 0.95rem;
    }

    .card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      border: 1px solid #e2e8f0;
      padding: 1.5rem;
      margin-bottom: 1.5rem;
    }

    .card h2 {
      margin: 0 0 1rem 0;
      font-size: 1.1rem;
      font-weight: 600;
      color: #334155;
    }

    .form .form-row {
      margin-bottom: 1rem;
    }

    .form .form-row label {
      display: block;
      font-size: 0.8rem;
      font-weight: 500;
      color: #475569;
      margin-bottom: 0.35rem;
    }

    .form input,
    .form textarea {
      width: 100%;
      padding: 0.5rem 0.75rem;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.95rem;
      box-sizing: border-box;
    }

    .form input:focus,
    .form textarea:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }

    .form-row.two-cols {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }

    .form-actions {
      display: flex;
      gap: 0.75rem;
      margin-top: 1.25rem;
    }

    .btn {
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.9rem;
      font-weight: 500;
      cursor: pointer;
      border: none;
      transition: background 0.15s, color 0.15s;
    }

    .btn-primary {
      background: #2563eb;
      color: #fff;
    }

    .btn-primary:hover {
      background: #1d4ed8;
    }

    .btn-secondary {
      background: #f1f5f9;
      color: #475569;
    }

    .btn-secondary:hover {
      background: #e2e8f0;
    }

    .btn-edit {
      background: #e0f2fe;
      color: #0369a1;
    }

    .btn-edit:hover {
      background: #bae6fd;
    }

    .btn-danger {
      background: #fee2e2;
      color: #b91c1c;
    }

    .btn-danger:hover {
      background: #fecaca;
    }

    .btn-sm {
      padding: 0.35rem 0.65rem;
      font-size: 0.8rem;
      margin-right: 0.5rem;
    }

    .table-wrap {
      overflow-x: auto;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.9rem;
    }

    .data-table th,
    .data-table td {
      padding: 0.75rem;
      text-align: left;
      border-bottom: 1px solid #e2e8f0;
    }

    .data-table th {
      font-weight: 600;
      color: #475569;
      background: #f8fafc;
    }

    .data-table tbody tr:hover {
      background: #f8fafc;
    }

    .col-actions {
      white-space: nowrap;
    }

    .empty {
      margin: 0;
      color: #64748b;
      font-size: 0.95rem;
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

  ngOnInit(): void {}

  private getEmptyForm(): Partial<Evenement> & { startTime: string; endTime: string } {
    return {
      title: '',
      description: '',
      location: '',
      capacity: 0,
      startTime: '',
      endTime: '',
      organizerId: 0,
    };
  }

  protected saveEvent(): void {
    if (!this.form.title) return;
    if (this.editingEvent?.eventId != null) {
      const payload: Evenement = {
        eventId: this.editingEvent.eventId,
        title: this.form.title,
        description: this.form.description,
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
    this.form = {
      title: event.title,
      description: event.description ?? '',
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
