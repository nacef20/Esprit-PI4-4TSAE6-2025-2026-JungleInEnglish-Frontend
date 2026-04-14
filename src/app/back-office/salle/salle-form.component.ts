import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Salle } from '../../models/salle.model';
import { SalleService } from '../../services/salle.service';

@Component({
  selector: 'app-salle-form',
  templateUrl: './salle-form.component.html',
  styleUrls: ['./salle-form.component.scss']
})
export class SalleFormComponent implements OnInit {
  salle: Salle = { nom_salle: '', capacite: 0 };
  isEditMode = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private salleService: SalleService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.isLoading = true;
      this.salleService.getSalleById(+id).subscribe({
        next: (data) => {
          this.salle = data;
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Erreur lors du chargement de la salle';
          this.isLoading = false;
        }
      });
    }
  }

  onSubmit(): void {
    this.isLoading = true;
    if (this.isEditMode && this.salle.id) {
      this.salleService.updateSalle(this.salle.id, this.salle).subscribe({
        next: () => {
          this.successMessage = 'Salle mise à jour avec succès';
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/admin/salle']), 1200);
        },
        error: () => {
          this.errorMessage = 'Erreur lors de la mise à jour';
          this.isLoading = false;
        }
      });
    } else {
      this.salleService.createSalle(this.salle).subscribe({
        next: () => {
          this.successMessage = 'Salle créée avec succès';
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/admin/salle']), 1200);
        },
        error: () => {
          this.errorMessage = 'Erreur lors de la création';
          this.isLoading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/admin/salle']);
  }
}
