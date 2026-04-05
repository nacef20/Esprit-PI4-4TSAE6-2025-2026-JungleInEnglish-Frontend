import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Complaint } from '../../models/complaint.model';
import { ComplaintResponse } from '../../models/response.model';
import { ComplaintService } from '../../services/complaint.service';
import { ComplaintResponseService } from '../../services/complaint-response.service';

@Component({
  selector: 'app-complaint-detail',
  templateUrl: './complaint-detail.component.html',
  styleUrls: ['./complaint-detail.component.scss']
})
export class ComplaintDetailComponent implements OnInit {

  complaint: Complaint | null = null;
  responses: ComplaintResponse[] = [];
  complaintId!: number;
  loading = true;

  // Response form
  newResponseMessage = '';
  submittingResponse = false;
  rejectingComplaint = false;
  editingResponseId: number | null = null;
  editResponseMessage = '';

  successMessage = '';
  errorMessage = '';

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
        this.errorMessage = 'Complaint not found.';
        this.loading = false;
      }
    });
  }

  loadResponses(): void {
    this.responseService.getByComplaintId(this.complaintId).subscribe({
      next: (data) => {
        this.responses = data;
      },
      error: () => {
        // Silently handle - complaint might have no responses
      }
    });
  }

  addResponse(): void {
    this.submittingResponse = true;
    const response: ComplaintResponse = { message: this.newResponseMessage };

    this.responseService.create(this.complaintId, response).subscribe({
      next: () => {
        this.newResponseMessage = '';
        this.submittingResponse = false;
        this.successMessage = 'Response added successfully!';
        this.loadResponses();
        this.loadComplaint(); // Reload to get updated status
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.submittingResponse = false;
        this.errorMessage = this.getBackendErrorMessage(error, 'Error while adding the response.');
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  startEditResponse(response: ComplaintResponse): void {
    this.editingResponseId = response.id!;
    this.editResponseMessage = response.message;
  }

  cancelEditResponse(): void {
    this.editingResponseId = null;
    this.editResponseMessage = '';
  }

  saveEditResponse(id: number): void {
    this.responseService.update(id, { message: this.editResponseMessage }).subscribe({
      next: () => {
        this.editingResponseId = null;
        this.editResponseMessage = '';
        this.successMessage = 'Response updated successfully!';
        this.loadResponses();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.errorMessage = this.getBackendErrorMessage(error, 'Error while updating.');
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  deleteResponse(id: number): void {
    if (confirm('Delete this response?')) {
      this.responseService.delete(id).subscribe({
        next: () => {
          this.successMessage = 'Response deleted.';
          this.loadResponses();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: () => {
          this.errorMessage = 'Error while deleting.';
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }

  rejectComplaint(): void {
    if (!this.complaint || this.complaint.status === 'REJETEE') {
      return;
    }

    if (!confirm('Reject this complaint?')) {
      return;
    }

    this.rejectingComplaint = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.complaintService.reject(this.complaintId).subscribe({
      next: (updatedComplaint) => {
        this.complaint = updatedComplaint;
        this.rejectingComplaint = false;
        this.successMessage = 'Complaint rejected successfully!';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        this.rejectingComplaint = false;
        this.errorMessage = this.getBackendErrorMessage(error, 'Error while rejecting complaint.');
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/complaints']);
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'Pending';
      case 'TRAITEE': return 'Resolved';
      case 'REJETEE': return 'Rejected';
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

  private getBackendErrorMessage(error: any, fallback: string): string {
    const payload = error?.error;

    if (typeof payload === 'string' && payload.trim()) {
      return payload;
    }

    if (payload && typeof payload === 'object') {
      if (typeof payload.error === 'string' && payload.error.trim()) {
        return payload.error;
      }

      for (const field of ['title', 'description', 'message']) {
        if (typeof payload[field] === 'string' && payload[field].trim()) {
          return payload[field];
        }
      }

      const firstMessage = Object.values(payload).find(
        (value) => typeof value === 'string' && value.trim()
      ) as string | undefined;

      if (firstMessage) {
        return firstMessage;
      }
    }

    return fallback;
  }
}

