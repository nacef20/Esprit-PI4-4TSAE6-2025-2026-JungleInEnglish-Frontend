import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, forkJoin, map, of } from 'rxjs';
import { Complaint, ComplaintStatus } from '../../models/complaint.model';
import { ComplaintSentimentResponse, ComplaintTone } from '../../models/complaint-sentiment.model';
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
  titleFilter = '';
  loading = false;
  currentPage = 1;
  pageSize = 6;
  totalElements = 0;
  totalPages = 0;
  deletingComplaintIds = new Set<number>();
  sentimentLoading = false;
  sentimentByComplaintId: Record<number, ComplaintSentimentResponse> = {};

  constructor(private complaintService: ComplaintService, private router: Router) {}

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
        this.applyFilters();
        this.loadSentimentAnalysis();
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

  loadSentimentAnalysis(): void {
    const complaintsWithId = this.filteredComplaints.filter((complaint) => complaint.id !== undefined);
    if (complaintsWithId.length === 0) {
      this.sentimentByComplaintId = {};
      this.sentimentLoading = false;
      return;
    }

    this.sentimentLoading = true;

    const requests = complaintsWithId.map((complaint) =>
      this.complaintService.analyzeSentiment({
        complaintId: complaint.id,
        title: complaint.title,
        description: complaint.description
      }).pipe(
        map((result) => ({
          id: complaint.id as number,
          result
        })),
        catchError(() => of({
          id: complaint.id as number,
          result: {
            complaintId: complaint.id,
            tone: 'NEUTRAL' as ComplaintTone,
            urgent: false,
            reason: 'Sentiment unavailable.'
          }
        }))
      )
    );

    forkJoin(requests).subscribe({
      next: (entries) => {
        const nextMap: Record<number, ComplaintSentimentResponse> = {};
        for (const entry of entries) {
          nextMap[entry.id] = entry.result;
        }
        this.sentimentByComplaintId = nextMap;
        this.sentimentLoading = false;
      },
      error: () => {
        this.sentimentLoading = false;
      }
    });
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

  viewComplaint(id: number): void {
    this.router.navigate(['/admin/complaints', id]);
  }

  deleteComplaint(id: number, event?: Event): void {
    event?.stopPropagation();

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

  getSentiment(complaintId?: number): ComplaintSentimentResponse | null {
    if (!complaintId) {
      return null;
    }

    return this.sentimentByComplaintId[complaintId] ?? null;
  }

  getToneLabel(tone: ComplaintTone): string {
    switch (tone) {
      case 'POSITIVE':
        return 'Positive';
      case 'NEGATIVE':
        return 'Negative';
      default:
        return 'Neutral';
    }
  }

  getToneIcon(tone: ComplaintTone): string {
    switch (tone) {
      case 'POSITIVE':
        return 'sentiment_satisfied';
      case 'NEGATIVE':
        return 'sentiment_dissatisfied';
      default:
        return 'sentiment_neutral';
    }
  }
}

