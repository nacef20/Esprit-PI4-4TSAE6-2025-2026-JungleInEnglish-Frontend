import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Planning, PlanningStatus, PlanningType } from '../../../models/planning.model';
import { PlanningService } from '../../../services/planning.service';

@Component({
    selector: 'app-planning-list',
    templateUrl: './planning-list.component.html',
    styleUrls: ['./planning-list.component.scss']
})
export class PlanningListComponent implements OnInit {
    plannings: Planning[] = [];
    filteredPlannings: Planning[] = [];
    searchTerm: string = '';
    isLoading: boolean = false;
    errorMessage: string = '';
    selectedPlanning: Planning | null = null;
    showDeleteModal: boolean = false;

    // Pour les filtres
    filterStatus: string = 'ALL';
    filterType: string = 'ALL';

    // Enums pour le template
    planningStatuses = Object.values(PlanningStatus);
    planningTypes = Object.values(PlanningType);

    // PDF download
    pdfDate: string = '';
    isPdfLoading: boolean = false;

    constructor(
        private planningService: PlanningService,
        private router: Router,
        private http: HttpClient
    ) { }

    ngOnInit(): void {
        this.loadPlannings();
        this.pdfDate = this.todayString();
    }

    private todayString(): string {
        const d = new Date();
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    /**
     * Télécharger le PDF de l'emploi du temps.
     * @param date  YYYY-MM-DD. Si omis, utilise la date du sélecteur dans le header.
     */
    downloadPdf(date?: string | Date): void {
        const dateStr = date
            ? this.toYYYYMMDD(date)
            : this.pdfDate;
        if (!dateStr) return;

        this.isPdfLoading = true;
        const url = `http://localhost:8089/pi/plannings/advanced/pdf?date=${dateStr}`;

        this.http.get(url, { responseType: 'blob' }).subscribe({
            next: (blob) => {
                const link = document.createElement('a');
                link.href = URL.createObjectURL(blob);
                link.download = `emploi_du_temps_${dateStr}.pdf`;
                link.click();
                URL.revokeObjectURL(link.href);
                this.isPdfLoading = false;
            },
            error: () => {
                console.error('Erreur lors du téléchargement du PDF');
                this.isPdfLoading = false;
            }
        });
    }

    private toYYYYMMDD(date: string | Date): string {
        const d = new Date(date);
        if (isNaN(d.getTime())) return String(date);
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    /**
     * Charger tous les plannings
     */
    loadPlannings(): void {
        this.isLoading = true;
        this.errorMessage = '';

        this.planningService.getAllPlannings().subscribe({
            next: (data) => {
                this.plannings = data;
                this.filteredPlannings = data;
                this.isLoading = false;
            },
            error: (error) => {
                this.errorMessage = 'Erreur lors du chargement des plannings';
                console.error('Error loading plannings:', error);
                this.isLoading = false;
            }
        });
    }

    /**
     * Rechercher des plannings
     */
    onSearch(): void {
        if (!this.searchTerm.trim()) {
            this.applyFilters();
            return;
        }

        this.filteredPlannings = this.plannings.filter(planning =>
            planning.observations?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
            planning.statut?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
            planning.salle?.nom_salle?.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
    }

    /**
     * Appliquer les filtres
     */
    applyFilters(): void {
        this.filteredPlannings = this.plannings.filter(planning => {
            const statusMatch = this.filterStatus === 'ALL' || planning.statut === this.filterStatus;
            return statusMatch;
        });
    }

    /**
     * Réinitialiser les filtres
     */
    resetFilters(): void {
        this.searchTerm = '';
        this.filterStatus = 'ALL';
        this.filterType = 'ALL';
        this.filteredPlannings = this.plannings;
    }

    /**
     * Naviguer vers la création d'un planning
     */
    createPlanning(): void {
        this.router.navigate(['/admin/planning/create']);
    }

    /**
     * Naviguer vers l'édition d'un planning
     */
    editPlanning(id: number | undefined): void {
        if (id) {
            this.router.navigate(['/admin/planning/edit', id]);
        }
    }

    /**
     * Afficher le modal de confirmation de suppression
     */
    confirmDelete(planning: Planning): void {
        this.selectedPlanning = planning;
        this.showDeleteModal = true;
    }

    /**
     * Annuler la suppression
     */
    cancelDelete(): void {
        this.selectedPlanning = null;
        this.showDeleteModal = false;
    }

    /**
     * Supprimer un planning
     */
    deletePlanning(): void {
        if (this.selectedPlanning && this.selectedPlanning.idPlanningSession) {
            this.planningService.deletePlanning(this.selectedPlanning.idPlanningSession).subscribe({
                next: () => {
                    this.loadPlannings();
                    this.showDeleteModal = false;
                    this.selectedPlanning = null;
                },
                error: (error) => {
                    this.errorMessage = 'Erreur lors de la suppression du planning';
                    console.error('Error deleting planning:', error);
                    this.showDeleteModal = false;
                }
            });
        }
    }

    /**
     * Formater la date
     */
    formatDate(date: Date | string | undefined): string {
        if (!date) return '-';
        return new Date(date).toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    /**
     * Obtenir le libellé du statut
     */
    getStatusLabel(statut?: string): string {
        const labels: { [key: string]: string } = {
            'PLANIFIE': 'Planifié',
            'EN_COURS': 'En cours',
            'TERMINE': 'Terminé',
            'ANNULE': 'Annulé'
        };
        return statut ? (labels[statut] || statut) : '';
    }

    /**
     * Obtenir la classe CSS pour le statut
     */
    getStatusClass(status?: PlanningStatus | string): string {
        if (!status) return '';
        const statusClasses: { [key: string]: string } = {
            'SCHEDULED': 'status-scheduled',
            'IN_PROGRESS': 'status-in-progress',
            'COMPLETED': 'status-completed',
            'CANCELLED': 'status-cancelled',
            'PLANIFIE': 'status-scheduled',
            'EN_COURS': 'status-in-progress',
            'TERMINE': 'status-completed',
            'ANNULE': 'status-cancelled'
        };
        return statusClasses[status] || '';
    }

    /**
     * Obtenir la classe CSS pour le type
     */
    getTypeClass(type?: PlanningType | string): string {
        if (!type) return '';
        const typeClasses: { [key: string]: string } = {
            'COURSE': 'type-course',
            'EXAM': 'type-exam',
            'EVENT': 'type-event',
            'MEETING': 'type-meeting',
            'OTHER': 'type-other'
        };
        return typeClasses[type] || '';
    }
}
