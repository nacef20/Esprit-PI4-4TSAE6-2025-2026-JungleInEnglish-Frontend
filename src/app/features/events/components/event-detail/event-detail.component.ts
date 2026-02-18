import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { EvenementService } from '../../services/evenement.service';
import type { Evenement } from '../../models/evenement.model';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [RouterLink, DatePipe],
  template: `
    @if (event()) {
      <h1>{{ event()!.title }}</h1>
      <p>{{ event()!.description }}</p>
      <p><strong>Location:</strong> {{ event()!.location }}</p>
      <p><strong>Start:</strong> {{ event()!.startTime | date:'medium' }}</p>
      <p><strong>End:</strong> {{ event()!.endTime | date:'medium' }}</p>
      <p><strong>Capacity:</strong> {{ event()!.capacity }}</p>
      <a routerLink="/events">← Back to list</a>
    } @else if (loading()) {
      <p>Loading...</p>
    } @else {
      <p>Event not found.</p>
    }
  `,
})
export class EventDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly evenementService = inject(EvenementService);
  protected readonly event = signal<Evenement | null>(null);
  protected readonly loading = signal(true);

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
