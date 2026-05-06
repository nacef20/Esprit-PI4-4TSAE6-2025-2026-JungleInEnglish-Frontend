import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { Evenement, Participation } from '../../models/event.model';
import { AuthService } from '../../auth/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-event-gallery',
  templateUrl: './event-gallery.component.html',
  styleUrls: ['./event-gallery.component.scss']
})
export class EventGalleryComponent implements OnInit {
  events: Evenement[] = [];
  isLoading = true;
  user: any;
  lastParticipation: Participation | null = null;
  userParticipations: Map<number, number> = new Map(); // eventId -> participationId

  constructor(public eventService: EventService, private authService: AuthService) {}

  ngOnInit(): void {
    this.user = this.authService.getUserInfo();
    this.loadEvents();
    if (this.authService.isLoggedIn() && this.user?.id) {
      this.loadUserParticipations();
    }
  }

  loadEvents(): void {
    this.isLoading = true;
    this.eventService.getEvents().subscribe({
      next: (data) => {
        this.events = data;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  loadUserParticipations(): void {
    if (!this.user?.id) return;
    this.eventService.getParticipationsByUser(this.user.id).subscribe({
      next: (participations) => {
        this.userParticipations.clear();
        participations.forEach(p => {
          if (p.event?.eventId) {
            this.userParticipations.set(p.event.eventId, p.participantId!);
          }
        });
      }
    });
  }

  isRegistered(eventId: number): boolean {
    return this.userParticipations.has(eventId);
  }

  register(eventId: number): void {
    if (!this.authService.isLoggedIn()) {
      this.authService.login();
      return;
    }

    if (this.user && this.user.id) {
      this.eventService.register(this.user.id, eventId).subscribe({
        next: (p) => {
          this.lastParticipation = p;
          this.loadUserParticipations();
          this.loadEvents();
          Swal.fire({
            title: 'Félicitations !',
            text: 'Votre inscription est confirmée. Voici votre ticket.',
            icon: 'success',
            timer: 3000,
            showConfirmButton: false
          });
        },
        error: (err) => Swal.fire('Erreur', err.error?.message || 'Erreur lors de l\'inscription', 'error')
      });
    }
  }

  unregister(eventId: number): void {
    const participationId = this.userParticipations.get(eventId);
    if (!participationId) return;

    Swal.fire({
      title: 'Êtes-vous sûr ?',
      text: "Voulez-vous vraiment annuler votre participation à cet événement ?",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#10b981',
      cancelButtonColor: '#ef4444',
      confirmButtonText: 'Oui, annuler',
      cancelButtonText: 'Non, garder'
    }).then((result) => {
      if (result.isConfirmed) {
        this.eventService.unregister(participationId).subscribe({
          next: () => {
            this.userParticipations.delete(eventId);
            this.loadEvents();
            Swal.fire('Annulé !', 'Votre inscription a été annulée avec succès.', 'success');
          },
          error: () => Swal.fire('Erreur', 'Impossible d\'annuler l\'inscription.', 'error')
        });
      }
    });
  }

  openInfo(event: Evenement): void {
    Swal.fire({
      title: event.title,
      html: `
        <div class="text-left space-y-4">
          <img src="${this.eventService.getMediaUrl(event.imageUrl)}" class="w-full h-48 object-cover rounded-xl mb-4 shadow-sm">
          <p class="text-gray-600">${event.description || 'Aucune description disponible.'}</p>
          <div class="grid grid-cols-2 gap-4 text-sm bg-gray-50 p-4 rounded-xl">
            <div>
              <p class="text-emerald-600 font-bold">Niveau</p>
              <p>${event.level}</p>
            </div>
            <div>
              <p class="text-emerald-600 font-bold">Lieu</p>
              <p>${event.location}</p>
            </div>
            <div>
              <p class="text-emerald-600 font-bold">Date</p>
              <p>${new Date(event.startTime).toLocaleDateString()}</p>
            </div>
            <div>
              <p class="text-emerald-600 font-bold">Heure</p>
              <p>${new Date(event.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</p>
            </div>
          </div>
        </div>
      `,
      showCloseButton: true,
      confirmButtonText: 'Fermer',
      confirmButtonColor: '#10b981',
      width: '600px',
      customClass: {
        container: 'premium-swal'
      }
    });
  }

  getQrCodeSrc(qrCodeUrl: string | undefined): string {
    if (!qrCodeUrl) return '';
    if (qrCodeUrl.startsWith('data:')) return qrCodeUrl;
    return 'data:image/png;base64,' + qrCodeUrl;
  }
}
