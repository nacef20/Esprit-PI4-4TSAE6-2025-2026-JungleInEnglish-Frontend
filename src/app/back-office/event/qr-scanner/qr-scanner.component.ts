import { Component } from '@angular/core';
import { EventService } from '../../../services/event.service';
import { BarcodeFormat } from '@zxing/library';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-qr-scanner',
  templateUrl: './qr-scanner.component.html',
  styleUrls: ['./qr-scanner.component.scss']
})
export class QrScannerComponent {
  scannedResult: any = null;
  isLoading = false;

  // Token input
  tokenInput = '';
  errorMessage = '';

  constructor(private eventService: EventService) {}

  verifyToken() {
    this.errorMessage = '';
    const t = this.tokenInput.trim();
    if (!t) {
      this.errorMessage = 'Veuillez entrer un token';
      return;
    }

    this.isLoading = true;
    this.scannedResult = null;

    this.eventService.getParticipationByToken(t).subscribe({
      next: (participation) => {
        this.scannedResult = participation;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || err.error?.message || 'Token invalide ou non trouvé.';
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: this.errorMessage,
          confirmButtonColor: '#ef4444'
        });
      }
    });
  }

  confirmCheckIn() {
    if (!this.tokenInput.trim()) return;
    
    this.isLoading = true;
    this.eventService.scanQrCode(this.tokenInput.trim()).subscribe({
      next: (participation) => {
        this.scannedResult = participation;
        this.isLoading = false;
        Swal.fire({
          icon: 'success',
          title: 'Check-in Réussi !',
          text: `Le participant a été marqué comme présent.`,
          confirmButtonColor: '#10b981'
        });
      },
      error: (err) => {
        this.isLoading = false;
        Swal.fire({
          icon: 'error',
          title: 'Erreur Check-in',
          text: err.error?.error || err.error?.message || 'Code QR invalide ou déjà utilisé.',
          confirmButtonColor: '#ef4444'
        });
      }
    });
  }

  reset() {
    this.scannedResult = null;
    this.tokenInput = '';
    this.errorMessage = '';
  }
}
