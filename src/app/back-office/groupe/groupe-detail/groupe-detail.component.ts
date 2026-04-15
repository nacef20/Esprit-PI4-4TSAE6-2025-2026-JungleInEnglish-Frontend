import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Groupe } from '../../../models/groupe.model';
import { GroupeService } from '../../../services/groupe.service';

@Component({
  selector: 'app-groupe-detail',
  templateUrl: './groupe-detail.component.html',
  styleUrls: ['./groupe-detail.component.scss']
})
export class GroupeDetailComponent implements OnInit {
  groupe: Groupe | null = null;
  loading = false;
  error: string | null = null;
  activeTab: 'details' | 'members' = 'details';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private groupeService: GroupeService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadGroupe(id);
    }

    const tab = this.route.snapshot.queryParamMap.get('tab');
    if (tab === 'members') {
      this.activeTab = 'members';
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
        this.error = 'Erreur lors du chargement du groupe';
        this.loading = false;
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

    return new Date(date).toLocaleDateString('fr-FR', {
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
}