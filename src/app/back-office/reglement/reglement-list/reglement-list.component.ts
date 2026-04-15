import { Component, OnInit } from '@angular/core';
import { ReglementGroupeService } from '../../../services/reglement-groupe.service';
import { ReglementGroupe, CategorieReglement } from '../../../models/reglement-groupe.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-reglement-list',
  templateUrl: './reglement-list.component.html',
  styleUrls: ['./reglement-list.component.scss'],
  standalone: false
})
export class ReglementListComponent implements OnInit {
  reglements: ReglementGroupe[] = [];
  filteredReglements: ReglementGroupe[] = [];
  loading = false;
  error: string | null = null;
  searchTerm = '';
  selectedCategorie: CategorieReglement | 'ALL' = 'ALL';
  categories = Object.values(CategorieReglement);

  showDeleteModal = false;
  reglementToDelete: number | null = null;

  constructor(
    private reglementService: ReglementGroupeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReglements();
  }

  loadReglements(): void {
    this.loading = true;
    this.reglementService.getAllReglements().subscribe({
      next: (data) => {
        this.reglements = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error while loading rules';
        this.loading = false;
        console.error(err);
      }
    });
  }

  applyFilters(): void {
    this.filteredReglements = this.reglements.filter(r => {
      const matchesSearch = r.titre.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                          r.codeReglement.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesCategorie = this.selectedCategorie === 'ALL' || r.categorie === this.selectedCategorie;
      return matchesSearch && matchesCategorie;
    });
  }

  onSearch(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  onAdd(): void {
    this.router.navigate(['/admin/reglement/add']);
  }

  onEdit(id: number): void {
    this.router.navigate(['/admin/reglement/edit', id]);
  }

  confirmDelete(id: number): void {
    this.reglementToDelete = id;
    this.showDeleteModal = true;
  }

  onDelete(): void {
    if (this.reglementToDelete) {
      this.reglementService.deleteReglement(this.reglementToDelete).subscribe({
        next: () => {
          this.reglements = this.reglements.filter(r => r.idReglement !== this.reglementToDelete);
          this.applyFilters();
          this.showDeleteModal = false;
          this.reglementToDelete = null;
        },
        error: (err) => {
          this.error = 'Error while deleting the rule';
          this.showDeleteModal = false;
          console.error(err);
        }
      });
    }
  }

  toggleStatus(reglement: ReglementGroupe): void {
    if (!reglement.idReglement) return;

    const action = reglement.estActif ? 
      this.reglementService.desactiverReglement(reglement.idReglement) : 
      this.reglementService.activerReglement(reglement.idReglement);

    action.subscribe({
      next: (updated) => {
        const index = this.reglements.findIndex(r => r.idReglement === updated.idReglement);
        if (index !== -1) {
          this.reglements[index] = updated;
          this.applyFilters();
        }
      },
      error: (err) => {
        this.error = 'Error while changing status';
        console.error(err);
      }
    });
  }

  getCategoryLabel(category: string): string {
    const labels: { [key: string]: string } = {
      PRESENCE: 'Attendance',
      RETARD: 'Tardiness',
      PARTICIPATION: 'Participation',
      COMPORTEMENT: 'Behavior',
      DEVOIRS: 'Homework',
      MATERIEL: 'Materials',
      AUTRE: 'Other'
    };
    return labels[category] || category;
  }

  getSanctionLabel(sanction?: string): string {
    const labels: { [key: string]: string } = {
      AVERTISSEMENT: 'Warning',
      EXCLUSION_TEMPORAIRE: 'Temporary exclusion',
      EXCLUSION_DEFINITIVE: 'Permanent exclusion',
      AUTRE: 'Other'
    };
    return sanction ? (labels[sanction] || sanction) : '-';
  }
}
