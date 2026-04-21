import { Component, OnInit } from '@angular/core';
import { EquilibrageService } from '../../../services/equilibrage.service';
import { PlanEquilibrageDTO } from '../../../models/equilibrage.model';

@Component({
  selector: 'app-equilibrage-groupes',
  templateUrl: './equilibrage-groupes.component.html',
  styleUrls: ['./equilibrage-groupes.component.scss'],
  standalone: false
})
export class EquilibrageGroupesComponent implements OnInit {
  niveauxEligibles: number[] = [];
  selectedNiveau: number | null = null;
  seuilTolerance = 2;

  plan: PlanEquilibrageDTO | null = null;
  generating = false;
  executing = false;
  error: string | null = null;
  successMessage: string | null = null;

  constructor(private equilibrageService: EquilibrageService) {}

  ngOnInit(): void {
    this.loadNiveaux();
  }

  loadNiveaux(): void {
    this.equilibrageService.getNiveauxEligibles().subscribe({
      next: (data) => {
        this.niveauxEligibles = data;
        if (data.length > 0) {
          this.selectedNiveau = data[0];
        }
      },
      error: (err) => {
        this.error = err.message;
      }
    });
  }

  genererPlan(): void {
    if (!this.selectedNiveau) {
      this.error = 'Please select a level.';
      return;
    }
    this.generating = true;
    this.error = null;
    this.successMessage = null;

    this.equilibrageService.genererPlan(this.selectedNiveau, this.seuilTolerance).subscribe({
      next: (data) => {
        this.plan = data;
        this.generating = false;
      },
      error: (err) => {
        this.error = err.message;
        this.generating = false;
      }
    });
  }

  executerPlan(): void {
    if (!this.selectedNiveau) return;
    this.executing = true;
    this.error = null;

    this.equilibrageService.executerPlan(this.selectedNiveau, this.seuilTolerance).subscribe({
      next: (data) => {
        this.plan = data;
        this.successMessage = data.message;
        this.executing = false;
      },
      error: (err) => {
        this.error = err.message;
        this.executing = false;
      }
    });
  }

  getAmeliorationClass(): string {
    if (!this.plan) return '';
    if (this.plan.ameliorationPourcent >= 50) return 'amelioration-excellent';
    if (this.plan.ameliorationPourcent >= 20) return 'amelioration-bon';
    return 'amelioration-faible';
  }
}
