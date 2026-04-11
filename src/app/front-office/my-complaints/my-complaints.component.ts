import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Complaint, ComplaintStatus } from '../../models/complaint.model';
import { ComplaintService } from '../../services/complaint.service';

@Component({
  selector: 'app-my-complaints',
  templateUrl: './my-complaints.component.html',
  styleUrls: ['./my-complaints.component.scss']
})
export class MyComplaintsComponent implements OnInit {

  complaints: Complaint[] = [];
  filteredComplaints: Complaint[] = [];
  statusFilter: ComplaintStatus | '' = '';
  titleFilter = '';
  loading = false;
  currentPage = 1;
  pageSize = 5;
  totalElements = 0;
  totalPages = 0;
  deletingComplaintIds = new Set<number>();

  constructor(
    private complaintService: ComplaintService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadComplaints();
  }

  loadComplaints(): void {
    this.loading = true;
    const request$ = this.complaintService.getPaged(
      this.currentPage - 1,
      this.pageSize,
      this.statusFilter,
      this.titleFilter
    );

    request$.subscribe({
      next: (pageData) => {
        this.complaints = pageData.content;
        this.totalElements = pageData.totalElements;
        this.totalPages = pageData.totalPages;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    this.filteredComplaints = [...this.complaints];
  }

  onFilterStatus(status: string): void {
    this.statusFilter = status as ComplaintStatus | '';
    this.currentPage = 1;
    this.loadComplaints();
  }

  onTitleSearch(): void {
    this.currentPage = 1;
    this.loadComplaints();
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadComplaints();
    }
  }

  newComplaint(): void {
    this.router.navigate(['/my-complaints/new']);
  }

  viewComplaint(id: number): void {
    this.router.navigate(['/my-complaints', id]);
  }

  deleteComplaint(id: number, event: Event): void {
    event.stopPropagation();

    const confirmed = window.confirm('Are you sure you want to delete this complaint?');
    if (!confirmed) {
      return;
    }

    const wasLastItemOnPage = this.filteredComplaints.length === 1 && this.currentPage > 1;

    this.deletingComplaintIds.add(id);
    this.complaintService.delete(id).subscribe({
      next: () => {
        if (wasLastItemOnPage) {
          this.currentPage -= 1;
        }
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

