import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
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
  downloadError = '';

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

  downloadComplaintAttachment(): void {
    if (!this.complaint?.id) {
      return;
    }

    this.downloadError = '';
    this.complaintService.downloadAttachment(this.complaint.id).subscribe({
      next: (response) => {
        const fallbackName = this.complaint?.attachmentFileName || 'complaint-attachment';
        this.triggerDownloadFromResponse(response, fallbackName);
      },
      error: () => {
        this.downloadError = 'Unable to download complaint attachment.';
      }
    });
  }

  downloadResponseAttachment(responseItem: ComplaintResponse): void {
    if (!responseItem.id) {
      return;
    }

    this.downloadError = '';
    this.responseService.downloadAttachment(responseItem.id).subscribe({
      next: (response) => {
        const fallbackName = responseItem.attachmentFileName || `response-${responseItem.id}-attachment`;
        this.triggerDownloadFromResponse(response, fallbackName);
      },
      error: () => {
        this.downloadError = 'Unable to download response attachment.';
      }
    });
  }

  private triggerDownloadFromResponse(response: HttpResponse<Blob>, fallbackName: string): void {
    if (!response.body) {
      return;
    }

    const header = response.headers.get('content-disposition') || '';
    const fileName = this.extractFilenameFromDisposition(header) || fallbackName;
    const blobUrl = URL.createObjectURL(response.body);
    const anchor = document.createElement('a');
    anchor.href = blobUrl;
    anchor.download = fileName;
    anchor.click();
    URL.revokeObjectURL(blobUrl);
  }

  private extractFilenameFromDisposition(contentDisposition: string): string {
    const utf8Match = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition);
    if (utf8Match?.[1]) {
      return decodeURIComponent(utf8Match[1]);
    }

    const simpleMatch = /filename="?([^";]+)"?/i.exec(contentDisposition);
    return simpleMatch?.[1] ?? '';
  }
}

