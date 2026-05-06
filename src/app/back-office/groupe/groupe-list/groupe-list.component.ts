import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Groupe, StatutGroupe, TypeGroupe } from '../../../models/groupe.model';
import { GroupeService } from '../../../services/groupe.service';

@Component({
  selector: 'app-groupe-list',
  templateUrl: './groupe-list.component.html',
  styleUrls: ['./groupe-list.component.scss']
})
export class GroupeListComponent implements OnInit {

  groupes: Groupe[] = [];
  filteredGroupes: Groupe[] = [];
  searchTerm: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  selectedGroupe: Groupe | null = null;
  showDeleteModal: boolean = false;

  filterStatut: string = 'ALL';
  filterType: string = 'ALL';

  statutValues = Object.values(StatutGroupe);
  typeValues = Object.values(TypeGroupe);

  constructor(
    private groupeService: GroupeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadGroupes();
  }

  loadGroupes(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.groupeService.getAllGroupes().subscribe({
      next: (data) => {
        this.groupes = data;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error while loading groups';
        console.error('Error loading groupes:', error);
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    this.filteredGroupes = this.groupes.filter(g => {
      const statutMatch = this.filterStatut === 'ALL' || g.statut === this.filterStatut;
      const typeMatch = this.filterType === 'ALL' || g.type === this.filterType;
      const searchMatch = !this.searchTerm.trim() ||
        g.nom?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        g.codeGroupe?.toLowerCase().includes(this.searchTerm.toLowerCase());
      return statutMatch && typeMatch && searchMatch;
    });
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.filterStatut = 'ALL';
    this.filterType = 'ALL';
    this.filteredGroupes = this.groupes;
  }

  createGroupe(): void {
    this.router.navigate(['/admin/groupe/create']);
  }

  viewDetails(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/admin/groupe/details', id]);
    }
  }

  editGroupe(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/admin/groupe/edit', id]);
    }
  }

  confirmDelete(groupe: Groupe): void {
    this.selectedGroupe = groupe;
    this.showDeleteModal = true;
  }

  cancelDelete(): void {
    this.selectedGroupe = null;
    this.showDeleteModal = false;
  }

  deleteGroupe(): void {
    if (this.selectedGroupe && this.selectedGroupe.idGroupe) {
      this.groupeService.deleteGroupe(this.selectedGroupe.idGroupe).subscribe({
        next: () => {
          this.loadGroupes();
          this.showDeleteModal = false;
          this.selectedGroupe = null;
        },
        error: (error) => {
          this.errorMessage = 'Error while deleting the group';
          console.error('Error deleting groupe:', error);
          this.showDeleteModal = false;
        }
      });
    }
  }

  getStatutClass(statut?: string): string {
    const classes: { [key: string]: string } = {
      'OUVERT': 'statut-ouvert',
      'COMPLET': 'statut-complet',
      'EN_COURS': 'statut-en-cours',
      'TERMINE': 'statut-termine',
      'ANNULE': 'statut-annule',
      'ARCHIVE': 'statut-archive'
    };
    return statut ? (classes[statut] || '') : '';
  }

  getTypeClass(type?: string): string {
    const classes: { [key: string]: string } = {
      'ETUDIANT': 'type-etudiant',
      'CLASSE': 'type-classe',
      'ADMINISTRATION': 'type-administration'
    };
    return type ? (classes[type] || '') : '';
  }

  formatDate(date?: string): string {
    if (!date) return '-';
    return new Date(date).toLocaleDateString('en-GB', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getCapacitePercent(groupe: Groupe): number {
    if (!groupe.capaciteMax || groupe.capaciteMax === 0) return 0;
    return Math.round(((groupe.capaciteActuelle || 0) / groupe.capaciteMax) * 100);
  }

  getStatusLabel(status?: string): string {
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

  getTypeLabel(type?: string): string {
    const labels: { [key: string]: string } = {
      ETUDIANT: 'Student',
      CLASSE: 'Class',
      ADMINISTRATION: 'Administration'
    };
    return type ? (labels[type] || type) : '-';
  }
}
