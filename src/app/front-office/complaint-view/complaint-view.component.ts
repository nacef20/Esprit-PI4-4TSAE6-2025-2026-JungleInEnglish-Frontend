import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Complaint } from '../../models/complaint.model';
import { ComplaintResponse } from '../../models/response.model';
import { ComplaintService } from '../../services/complaint.service';
import { ComplaintResponseService } from '../../services/complaint-response.service';

@Component({
  selector: 'app-complaint-view',
  templateUrl: './complaint-view.component.html',
  styleUrls: ['./complaint-view.component.scss']
})
export class ComplaintViewComponent implements OnInit {

  complaint: Complaint | null = null;
  responses: ComplaintResponse[] = [];
  complaintId!: number;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private complaintService: ComplaintService,
    private responseService: ComplaintResponseService
  ) {}

  ngOnInit(): void {
    this.complaintId = +this.route.snapshot.params['id'];
    this.loadComplaint();
    this.loadResponses();
  }

  loadComplaint(): void {
    this.loading = true;
    this.complaintService.getById(this.complaintId).subscribe({
      next: (data) => {
        this.complaint = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadResponses(): void {
    this.responseService.getByComplaintId(this.complaintId).subscribe({
      next: (data) => this.responses = data
    });
  }

  goBack(): void {
    this.router.navigate(['/my-complaints']);
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'En attente';
      case 'TRAITEE': return 'Traitée';
      case 'REJETEE': return 'Rejetée';
      default: return status;
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'hourglass_top';
      case 'TRAITEE': return 'check_circle';
      case 'REJETEE': return 'cancel';
      default: return 'help';
    }
  }
}
