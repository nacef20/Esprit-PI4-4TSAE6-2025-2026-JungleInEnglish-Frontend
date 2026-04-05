import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Complaint } from '../../models/complaint.model';
import { ComplaintService } from '../../services/complaint.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-complaint-list',
  templateUrl: './complaint-list.component.html',
  styleUrls: ['./complaint-list.component.scss']
})
export class ComplaintListComponent implements OnInit, OnDestroy {

  complaints: Complaint[] = [];
  filteredComplaints: Complaint[] = [];
  searchTerm = '';
  statusFilter = '';
  loading = false;
  deletingComplaintIds = new Set<number>();
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(private complaintService: ComplaintService, private router: Router) {}

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(term => {
      this.searchTerm = term;
      this.applyFilters();
    });
    this.loadComplaints();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadComplaints(): void {
    this.loading = true;
    this.complaintService.getAll().subscribe({
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
    let result = [...this.complaints];

    if (this.statusFilter) {
      result = result.filter(c => c.status === this.statusFilter);
    }

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(c =>
        c.title.toLowerCase().includes(term) ||
        c.description.toLowerCase().includes(term)
      );
    }

    this.filteredComplaints = result;
  }

  onSearch(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchSubject.next(value);
  }

  onFilterStatus(status: string): void {
    this.statusFilter = status;
    this.applyFilters();
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
        this.complaints = this.complaints.filter(c => c.id !== id);
        this.applyFilters();
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

