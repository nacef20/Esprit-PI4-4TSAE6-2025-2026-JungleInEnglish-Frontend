import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormateurEnrichi, Groupe, GroupeEnrichi } from '../../../models/groupe.model';
import { GroupeService } from '../../../services/groupe.service';

@Component({
  selector: 'app-groupe-detail',
  templateUrl: './groupe-detail.component.html',
  styleUrls: ['./groupe-detail.component.scss']
})
export class GroupeDetailComponent implements OnInit {
  groupe: Groupe | null = null;
  groupeEnrichi: GroupeEnrichi | null = null;
  loading = false;
  loadingEnriched = false;
  error: string | null = null;
  enrichedError: string | null = null;
  activeTab: 'details' | 'members' | 'formateurs' = 'details';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private groupeService: GroupeService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadGroupe(id);
      this.loadGroupeEnrichi(id);
    }

    const tab = this.route.snapshot.queryParamMap.get('tab');
    if (tab === 'members') {
      this.activeTab = 'members';
    } else if (tab === 'formateurs') {
      this.activeTab = 'formateurs';
    }
  }

  loadGroupe(id: number): void {
    this.loading = true;
    this.error = null;
    this.groupeService.getGroupeById(id).subscribe({
      next: data => {
        this.groupe = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error while loading the group';
        this.loading = false;
      }
    });
  }

  loadGroupeEnrichi(id: number): void {
    this.loadingEnriched = true;
    this.enrichedError = null;
    this.groupeService.getGroupeEnrichi(id).subscribe({
      next: data => {
        this.groupeEnrichi = data;
        this.loadingEnriched = false;
      },
      error: () => {
        this.enrichedError = 'Error while loading enriched instructors';
        this.loadingEnriched = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/groupe']);
  }

  formatDate(date?: string): string {
    if (!date) {
      return '-';
    }

    return new Date(date).toLocaleDateString('en-GB', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getProgressPercent(): number {
    if (!this.groupe?.capaciteMax) {
      return 0;
    }

    return Math.round(((this.groupe.capaciteActuelle || 0) / this.groupe.capaciteMax) * 100);
  }

  getFormateurDisplayName(formateur: FormateurEnrichi): string {
    const fullName = [formateur.firstName, formateur.lastName].filter(Boolean).join(' ').trim();
    return fullName || formateur.username || `Instructor #${formateur.formateurId}`;
  }

  getStatusLabel(status?: string | null): string {
    const labels: { [key: string]: string } = {
      OUVERT: 'Open',
      COMPLET: 'Full',
      EN_COURS: 'In progress',
      TERMINE: 'Completed',
      ANNULE: 'Canceled',
      ARCHIVE: 'Archived'
    };
    return status ? (labels[status] || status) : '-';
  }

  getTypeLabel(type?: string | null): string {
    const labels: { [key: string]: string } = {
      ETUDIANT: 'Student',
      CLASSE: 'Class',
      ADMINISTRATION: 'Administration'
    };
    return type ? (labels[type] || type) : '-';
  }
}