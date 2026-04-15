import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { Evenement, Participation } from '../../models/event.model';
import { AuthService } from '../../auth/auth.service';

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

  constructor(public eventService: EventService, private authService: AuthService) {}

  ngOnInit(): void {
    this.user = this.authService.getUserInfo();
    this.loadEvents();
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

  register(eventId: number): void {
    if (!this.authService.isLoggedIn()) {
      this.authService.login();
      return;
    }

    if (this.user && this.user.id) {
      this.eventService.register(this.user.id, eventId).subscribe({
        next: (p) => {
          this.lastParticipation = p;
          alert('Inscription réussie ! Voici votre ticket.');
        },
        error: (err) => alert(err.error?.message || 'Erreur lors de l\'inscription')
      });
    }
  }

  getQrCodeSrc(qrCodeUrl: string | undefined): string {
    if (!qrCodeUrl) return '';
    if (qrCodeUrl.startsWith('data:')) return qrCodeUrl;
    return 'data:image/png;base64,' + qrCodeUrl;
  }
}
