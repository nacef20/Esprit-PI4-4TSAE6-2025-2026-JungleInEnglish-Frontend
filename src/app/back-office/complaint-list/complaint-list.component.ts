import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Complaint, ComplaintStatus } from '../../models/complaint.model';
import { ComplaintService } from '../../services/complaint.service';

@Component({
  selector: 'app-complaint-list',
  templateUrl: './complaint-list.component.html',
  styleUrls: ['./complaint-list.component.scss']
})
export class ComplaintListComponent implements OnInit {

  complaints: Complaint[] = [];
  filteredComplaints: Complaint[] = [];
  statusFilter: ComplaintStatus | '' = '';
  loading = false;
  deletingComplaintIds = new Set<number>();

  constructor(private complaintService: ComplaintService, private router: Router) {}

  ngOnInit(): void {
    this.loadComplaints();
  }

  loadComplaints(): void {
    this.loading = true;
    const request$ = this.statusFilter
      ? this.complaintService.getByStatus(this.statusFilter)
      : this.complaintService.getAll();

    request$.subscribe({
      next: (data) => {
        this.complaints = data;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredComplaints = [...this.complaints];
  }

  onFilterStatus(status: string): void {
    this.statusFilter = status as ComplaintStatus | '';
    this.loadComplaints();
  }

  viewComplaint(id: number): void {
    this.router.navigate(['/admin/complaints', id]);
  }

  deleteComplaint(id: number, event?: Event): void {
    event?.stopPropagation();

    const confirmed = window.confirm('Are you sure you want to delete this complaint?');
    if (!confirmed) {
      return;
    }

    this.deletingComplaintIds.add(id);
    this.complaintService.delete(id).subscribe({
      next: () => {
        this.loadComplaints();
        this.deletingComplaintIds.delete(id);
      },
      error: (error: HttpErrorResponse) => {
        this.deletingComplaintIds.delete(id);
        const message = this.getBackendErrorMessage(error) || 'Unable to delete complaint. Please try again.';
        alert(message);
      }
    });
  }

  isDeleting(id: number): boolean {
    return this.deletingComplaintIds.has(id);
  }

  private getBackendErrorMessage(error: HttpErrorResponse): string {
    const payload = error?.error;

    if (!payload) {
      return '';
    }

    if (typeof payload === 'string') {
      return payload;
    }

    if (typeof payload.message === 'string' && payload.message.trim()) {
      return payload.message;
    }

    return '';
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
}

