import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AsyncPipe, DatePipe } from '@angular/common';
import { EvenementService } from '../../services/evenement.service';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [RouterLink, AsyncPipe, DatePipe],
  template: `
    <h1>Events</h1>
    @if (events$ | async; as events) {
      <ul class="event-list">
        @for (event of events; track event.eventId) {
          <li>
            <a [routerLink]="['/events', event.eventId]">{{ event.title }}</a>
            <span class="meta">{{ event.location }} · {{ event.startTime | date:'short' }}</span>
          </li>
        }
      </ul>
      @if (events.length === 0) {
        <p>No events yet.</p>
      }
    } @else {
      <p>Loading...</p>
    }
  `,
  styles: `
    .event-list {
      list-style: none;
      padding: 0;
    }
    .event-list li {
      padding: 0.5rem 0;
      border-bottom: 1px solid #eee;
    }
    .meta { font-size: 0.875rem; color: #666; }
  `,
})
export class EventListComponent implements OnInit {
  private readonly evenementService = inject(EvenementService);
  protected readonly events$ = this.evenementService.getAll();

  ngOnInit(): void {}
}
