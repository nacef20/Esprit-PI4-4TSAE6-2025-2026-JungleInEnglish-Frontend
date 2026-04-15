import { Component, OnInit } from '@angular/core';
import { ObjectifPedagogiqueService } from '../../../services/objectif-pedagogique.service';
import { ObjectifPedagogique, StatutObjectif, CompetenceCible } from '../../../models/objectif-pedagogique.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-objectif-list',
  templateUrl: './objectif-list.component.html',
  styleUrls: ['./objectif-list.component.scss'],
  standalone: false
})
export class ObjectifListComponent implements OnInit {
  objectifs: ObjectifPedagogique[] = [];
  filteredObjectifs: ObjectifPedagogique[] = [];
  loading = false;
  error: string | null = null;
  searchTerm = '';
  selectedStatut: StatutObjectif | 'ALL' = 'ALL';
  selectedCompetence: CompetenceCible | 'ALL' = 'ALL';
  
  statuts = Object.values(StatutObjectif);
  competences = Object.values(CompetenceCible);

  showDeleteModal = false;
  objectifToDelete: number | null = null;

  constructor(
    private objectifService: ObjectifPedagogiqueService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadObjectifs();
  }

  loadObjectifs(): void {
    this.loading = true;
    this.objectifService.getAllObjectifs().subscribe({
      next: (data) => {
        this.objectifs = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des objectifs';
        this.loading = false;
        console.error(err);
      }
    });
  }

  applyFilters(): void {
    this.filteredObjectifs = this.objectifs.filter(o => {
      const matchesSearch = o.titre.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                          o.codeObjectif.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatut = this.selectedStatut === 'ALL' || o.statut === this.selectedStatut;
      const matchesCompetence = this.selectedCompetence === 'ALL' || o.competenceCible === this.selectedCompetence;
      return matchesSearch && matchesStatut && matchesCompetence;
    });
  }

  onSearch(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  onAdd(): void {
    this.router.navigate(['/admin/objectif/add']);
  }

  onEdit(id: number): void {
    this.router.navigate(['/admin/objectif/edit', id]);
  }

  confirmDelete(id: number): void {
    this.objectifToDelete = id;
    this.showDeleteModal = true;
  }

  onDelete(): void {
    if (this.objectifToDelete) {
      this.objectifService.deleteObjectif(this.objectifToDelete).subscribe({
        next: () => {
          this.objectifs = this.objectifs.filter(o => o.idObjectif !== this.objectifToDelete);
          this.applyFilters();
          this.showDeleteModal = false;
          this.objectifToDelete = null;
        },
        error: (err) => {
          this.error = 'Erreur lors de la suppression';
          this.showDeleteModal = false;
          console.error(err);
        }
      });
    }
  }

  marquerCommeAtteint(id: number): void {
    this.objectifService.marquerAtteint(id).subscribe({
      next: (updated) => {
        const index = this.objectifs.findIndex(o => o.idObjectif === updated.idObjectif);
        if (index !== -1) {
          this.objectifs[index] = updated;
          this.applyFilters();
        }
      },
      error: (err) => {
        this.error = "Erreur lors de la mise à jour de l'objectif";
        console.error(err);
      }
    });
  }

  getPriorityLabel(priorite: number): string {
    switch(priorite) {
      case 1: return 'Critique';
      case 2: return 'Haute';
      case 3: return 'Moyenne';
      case 4: return 'Basse';
      case 5: return 'Secondaire';
      default: return 'Inconnue';
    }
  }

  getPriorityClass(priorite: number): string {
    if (priorite <= 2) return 'priority-high';
    if (priorite === 3) return 'priority-medium';
    return 'priority-low';
  }
}
