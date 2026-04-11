import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { Salle } from '../../models/salle.model';
import { SalleService } from '../../services/salle.service';

@Component({
  selector: 'app-salle-list',
  templateUrl: './salle-list.component.html',
  styleUrls: ['./salle-list.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class SalleListComponent implements OnInit {
  salles: Salle[] = [];
  errorMessage = '';

  constructor(private salleService: SalleService, private router: Router) {}

  ngOnInit(): void {
    this.loadSalles();
  }

  loadSalles(): void {
    this.salleService.getAllSalles().subscribe({
      next: (data) => this.salles = data,
      error: () => this.errorMessage = 'Erreur lors du chargement des salles'
    });
  }

  editSalle(salle: Salle): void {
    this.router.navigate(['/admin/salle/edit', salle.id]);
  }

  deleteSalle(id?: number): void {
    if (!id) return;
    if (confirm('Supprimer cette salle ?')) {
      this.salleService.deleteSalle(id).subscribe({
        next: () => this.loadSalles(),
        error: () => this.errorMessage = 'Erreur lors de la suppression'
      });
    }
  }
}
