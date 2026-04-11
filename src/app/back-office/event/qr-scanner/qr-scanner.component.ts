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
  allowedFormats = [ BarcodeFormat.QR_CODE ];
  scannedResult: any = null;
  isScanning = true;
  isLoading = false;

  // Token → QR Code lookup
  tokenInput = '';
  tokenQrUrl: string | null = null;

  constructor(private eventService: EventService) {}

  onCodeResult(resultString: string) {
    if (this.isLoading) return;
    
    this.isLoading = true;
    this.isScanning = false;
    
    this.eventService.scanQrCode(resultString).subscribe({
      next: (participation) => {
        this.scannedResult = participation;
        this.isLoading = false;
        Swal.fire({
          icon: 'success',
          title: 'Check-in Réussi !',
          text: `Bienvenue, ${participation.userId} ! (ID Participation: ${participation.participantId})`,
          confirmButtonColor: '#10b981'
        });
      },
      error: (err) => {
        this.isLoading = false;
        Swal.fire({
          icon: 'error',
          title: 'Erreur Scan',
          text: err.error?.message || 'Code QR invalide ou déjà utilisé.',
          confirmButtonColor: '#ef4444'
        }).then(() => {
          this.isScanning = true;
        });
      }
    });
  }

  resumeScanning() {
    this.scannedResult = null;
    this.isScanning = true;
  }

  generateQrFromToken() {
    const t = this.tokenInput.trim();
    if (!t) return;
    // Use the free QR Server API — no library needed
    this.tokenQrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=224x224&data=${encodeURIComponent(t)}&format=png`;
  }

  downloadQr() {
    if (!this.tokenQrUrl) return;
    const a = document.createElement('a');
    a.href = this.tokenQrUrl;
    a.download = `ticket-${this.tokenInput.substring(0, 8)}.png`;
    a.target = '_blank';
    a.click();
  }
}
