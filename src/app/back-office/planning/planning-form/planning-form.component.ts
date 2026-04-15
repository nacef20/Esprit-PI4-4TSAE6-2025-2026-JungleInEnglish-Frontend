import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PlanningService } from '../../../services/planning.service';

@Component({
    selector: 'app-planning-form',
    templateUrl: './planning-form.component.html',
    styleUrls: ['./planning-form.component.scss']
})
export class PlanningFormComponent implements OnInit {

    titre: string = '';
    description: string = '';
    datePlanning: string = '';
    selectedStatut: string = 'PLANIFIE';
    selectedType: string = 'COURSE';
    selectedSalleId: number | null = null;
    selectedCreneauId: number | null = null;

    salles: any[] = [];
    creneaux: any[] = [];

    isEditMode: boolean = false;
    planningId: number | null = null;
    isLoading: boolean = false;
    errorMessage: string = '';
    successMessage: string = '';

    statuts: string[] = ['PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE', 'REPORTE'];
    types: string[] = ['COURSE', 'EXAM', 'EVENT', 'MEETING', 'OTHER'];

    constructor(
        private planningService: PlanningService,
        private router: Router,
        private route: ActivatedRoute
    ) {}

    ngOnInit(): void {
        this.loadSalles();
        this.loadCreneaux();

        this.route.params.subscribe(params => {
            if (params['id']) {
                this.isEditMode = true;
                this.planningId = +params['id'];
                this.loadPlanning(this.planningId);
            }
        });
    }

    loadSalles(): void {
        this.planningService.getAllSalles().subscribe({
            next: (data: any[]) => this.salles = data,
            error: () => this.errorMessage = 'Erreur chargement salles'
        });
    }

    loadCreneaux(): void {
        this.planningService.getAllCreneaux().subscribe({
            next: (data: any[]) => this.creneaux = data,
            error: () => this.errorMessage = 'Erreur chargement créneaux'
        });
    }

    loadPlanning(id: number): void {
        this.isLoading = true;
        this.planningService.getPlanningById(id).subscribe({
            next: (data: any) => {
                this.titre = data.observations || '';
                this.datePlanning = data.datePlanning || '';
                this.selectedStatut = data.statut || 'PLANIFIE';
                this.selectedSalleId = data.salle?.id || null;
                this.selectedCreneauId = data.creneau?.idCreneauHoraire || null;
                this.isLoading = false;
            },
            error: () => {
                this.errorMessage = 'Erreur chargement planning';
                this.isLoading = false;
            }
        });
    }

    validateForm(): boolean {
        this.errorMessage = '';
        if (!this.titre?.trim()) {
            this.errorMessage = 'Le titre est requis';
            return false;
        }
        if (!this.datePlanning) {
            this.errorMessage = 'La date est requise';
            return false;
        }
        if (!this.selectedSalleId) {
            this.errorMessage = 'Veuillez sélectionner une salle';
            return false;
        }
        return true;
    }

    onSubmit(): void {
        if (!this.validateForm()) return;

        this.isLoading = true;

        const payload: any = {
            datePlanning: this.datePlanning,
            statut: this.selectedStatut,
            observations: '[' + this.selectedType + '] ' + this.titre + ' - ' + (this.description || ''),
            groupeId: null,
            enseignantId: null,
            matiereId: null,
            salle: { id: Number(this.selectedSalleId) }
        };

        if (this.selectedCreneauId) {
            payload.creneau = { idCreneauHoraire: Number(this.selectedCreneauId) };
        }

        console.log('Payload envoyé:', JSON.stringify(payload, null, 2));

        if (this.isEditMode && this.planningId) {
            this.planningService.updatePlanning(this.planningId, payload).subscribe({
                next: () => {
                    this.successMessage = 'Planning mis à jour avec succès !';
                    this.isLoading = false;
                    setTimeout(() => this.router.navigate(['/admin/planning']), 1500);
                },
                error: (err: any) => {
                    this.errorMessage = 'Erreur mise à jour : ' + err.message;
                    this.isLoading = false;
                }
            });
        } else {
            this.planningService.createPlanning(payload).subscribe({
                next: () => {
                    this.successMessage = 'Planning créé avec succès !';
                    this.isLoading = false;
                    setTimeout(() => this.router.navigate(['/admin/planning']), 1500);
                },
                error: (err: any) => {
                    this.errorMessage = 'Erreur création : ' + err.message;
                    this.isLoading = false;
                }
            });
        }
    }

    goBack(): void {
        this.router.navigate(['/admin/planning']);
    }
}