import { Component, OnInit } from '@angular/core';
import { AnomalieService } from '../../../services/anomalie.service';
import { AlerteGroupeDTO } from '../../../models/anomalie.model';

@Component({
  selector: 'app-alertes-anomalies',
  templateUrl: './alertes-anomalies.component.html',
  styleUrls: ['./alertes-anomalies.component.scss'],
  standalone: false
})
export class AlertesAnomaliesComponent implements OnInit {
  alertes: AlerteGroupeDTO[] = [];
  filteredAlertes: AlerteGroupeDTO[] = [];
  activeFilter: string = 'ALL';

  loading = false;
  error: string | null = null;

  stats = { CRITIQUE: 0, WARNING: 0, INFO: 0, TOTAL: 0 };

  constructor(private anomalieService: AnomalieService) {}

  ngOnInit(): void {
    this.loadAlertes();
  }

  loadAlertes(): void {
    this.loading = true;
    this.error = null;
    this.anomalieService.scanComplet().subscribe({
      next: (data) => {
        this.alertes = data;
        this.stats.CRITIQUE = data.filter(a => a.severite === 'CRITIQUE').length;
        this.stats.WARNING = data.filter(a => a.severite === 'WARNING').length;
        this.stats.INFO = data.filter(a => a.severite === 'INFO').length;
        this.stats.TOTAL = data.length;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  setFilter(filter: string): void {
    this.activeFilter = filter;
    this.applyFilter();
  }

  applyFilter(): void {
    if (this.activeFilter === 'ALL') {
      this.filteredAlertes = [...this.alertes];
    } else {
      this.filteredAlertes = this.alertes.filter(a => a.severite === this.activeFilter);
    }
  }

  getSeveriteClass(severite: string): string {
    switch (severite) {
      case 'CRITIQUE': return 'badge-critique';
      case 'WARNING': return 'badge-warning';
      case 'INFO': return 'badge-info';
      default: return '';
    }
  }

  getSeveriteIcon(severite: string): string {
    switch (severite) {
      case 'CRITIQUE': return 'error';
      case 'WARNING': return 'warning';
      case 'INFO': return 'info';
      default: return 'help';
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'SANS_FORMATEUR': return 'person_off';
      case 'CAPACITE_INCOHERENTE': return 'data_alert';
      case 'OBJECTIF_EN_RETARD': return 'schedule';
      case 'SANS_OBJECTIF': return 'flag';
      default: return 'report';
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'SANS_FORMATEUR': return 'No instructor';
      case 'CAPACITE_INCOHERENTE': return 'Inconsistent capacity';
      case 'OBJECTIF_EN_RETARD': return 'Delayed objective';
      case 'SANS_OBJECTIF': return 'No objective';
      default: return type.replace(/_/g, ' ');
    }
  }
}
