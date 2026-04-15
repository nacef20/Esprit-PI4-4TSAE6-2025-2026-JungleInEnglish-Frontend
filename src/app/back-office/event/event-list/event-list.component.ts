import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';
import { AuthService } from '../../../auth/auth.service';
import { Evenement } from '../../../models/event.model';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.component.html',
  styleUrls: ['./event-list.component.scss']
})
export class EventListComponent implements OnInit {
  events: Evenement[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(public eventService: EventService, public authService: AuthService, private router: Router) {}

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

  viewParticipants(id: number): void {
    this.eventService.getParticipantsByEvent(id).subscribe({
      next: (participations) => {
        if (participations.length === 0) {
          Swal.fire('Participants', 'Aucun participant inscrit pour le moment.', 'info');
          return;
        }

        const participantsHtml = participations.map(p => 
          `<li style="margin-bottom: 8px;">User: <strong>${p.userId}</strong><br>
           <small>Date: ${p.registrationDate ? new Date(p.registrationDate as string).toLocaleString() : 'Inconnue'}</small> - 
           <span style="font-weight:bold; color:${p.present ? '#10b981' : '#f59e0b'}">
             ${p.present ? 'Présent' : 'En attente'}
           </span></li>`
        ).join('');

        Swal.fire({
          title: 'Liste des Participants',
          html: `<ul style="text-align: left; max-height: 60vh; overflow-y: auto; list-style-type: disc; padding-left: 20px;">${participantsHtml}</ul>`,
          confirmButtonColor: '#10b981',
          width: '500px'
        });
      },
      error: () => {
        Swal.fire('Erreur', 'Impossible de récupérer la liste des participants.', 'error');
      }
    });
  }

  createEvent(): void {
    this.router.navigate(['/admin/events/new']);
  }
}
