import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';
import { Evenement } from '../../../models/event.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.component.html',
  styleUrls: ['./event-list.component.scss']
})
export class EventListComponent implements OnInit {
  events: Evenement[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(public eventService: EventService, private router: Router) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.isLoading = true;
    this.eventService.getEvents().subscribe({
      next: (data) => {
        this.events = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement des événements.';
        this.isLoading = false;
      }
    });
  }

  deleteEvent(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet événement ?')) {
      this.eventService.deleteEvent(id).subscribe({
        next: () => this.loadEvents(),
        error: () => alert('Erreur lors de la suppression')
      });
    }
  }

  editEvent(id: number): void {
    this.router.navigate(['/admin/events/edit', id]);
  }

  createEvent(): void {
    this.router.navigate(['/admin/events/new']);
  }
}
