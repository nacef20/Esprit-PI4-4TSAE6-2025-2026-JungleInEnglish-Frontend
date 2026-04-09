import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Complaint } from '../../models/complaint.model';
import { ComplaintResponse } from '../../models/response.model';
import { ComplaintService } from '../../services/complaint.service';
import { ComplaintResponseService } from '../../services/complaint-response.service';
import { ResponseAiAssistMode } from '../../models/response-ai-assist.model';

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
  aiResponseLoading = false;
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

  generateResponseText(): void {
    this.runAiAssistForResponse('GENERATE');
  }

  improveResponseText(): void {
    this.runAiAssistForResponse('IMPROVE');
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

  private runAiAssistForResponse(mode: ResponseAiAssistMode): void {
    if (this.aiResponseLoading || this.submittingResponse || !this.complaint) {
      return;
    }

    this.aiResponseLoading = true;
    this.errorMessage = '';
    const currentDraft = (this.newResponseMessage ?? '').trim();
    const effectiveMode: ResponseAiAssistMode = mode === 'IMPROVE' && !currentDraft ? 'GENERATE' : mode;

    this.responseService.assistTextWithAi({
      complaintTitle: this.complaint.title,
      complaintDescription: this.complaint.description,
      draftResponse: currentDraft,
      mode: effectiveMode
    }).subscribe({
      next: (result) => {
        const normalized = this.normalizeResponseAiMessage(result.suggestedMessage || '');
        let nextMessage = (normalized || currentDraft).trim();

        if (effectiveMode === 'IMPROVE' && nextMessage === currentDraft) {
          nextMessage = this.forceImproveText(currentDraft, 'Thank you for your patience and understanding.');
        }

        this.newResponseMessage = nextMessage;
        this.aiResponseLoading = false;
      },
      error: (error) => {
        this.aiResponseLoading = false;
        this.errorMessage = this.getBackendErrorMessage(
          error,
          'AI assistance is unavailable right now. Please try again.'
        );
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  private normalizeResponseAiMessage(value: string): string {
    const raw = (value ?? '').trim();
    if (!raw) {
      return '';
    }

    const parsed = this.tryParseAiJson(raw);
    const parsedMessage = parsed?.['message'];
    if (typeof parsedMessage === 'string' && parsedMessage.trim()) {
      return this.cleanupJsonArtifacts(parsedMessage, 'message');
    }

    const lenientMessage = this.extractJsonFieldLenient(raw, 'message')
      || this.extractJsonFieldLenient(raw, 'description')
      || this.extractJsonFieldLenient(raw, 'title');

    return this.cleanupJsonArtifacts(lenientMessage || raw, 'message');
  }

  private tryParseAiJson(value: string): Record<string, unknown> | null {
    const trimmed = (value ?? '').trim();
    const start = trimmed.indexOf('{');
    const end = trimmed.lastIndexOf('}');

    if (start < 0 || end <= start) {
      return null;
    }

    const jsonCandidate = trimmed.slice(start, end + 1);
    try {
      const parsed = JSON.parse(jsonCandidate);
      return parsed && typeof parsed === 'object' ? parsed as Record<string, unknown> : null;
    } catch {
      return null;
    }
  }

  private extractJsonFieldLenient(value: string, fieldName: string): string {
    if (!value) {
      return '';
    }

    const source = value
      .replace(/```json/gi, '')
      .replace(/```/g, '')
      .trim();

    const fieldPattern = new RegExp(`["']?${fieldName}["']?\\s*:\\s*`, 'i');
    const match = fieldPattern.exec(source);
    if (!match || match.index === undefined) {
      return '';
    }

    let index = match.index + match[0].length;
    while (index < source.length && /\s/.test(source[index])) {
      index++;
    }

    if (index >= source.length) {
      return '';
    }

    const quote = source[index] === '"' || source[index] === '\'' ? source[index] : '';
    if (quote) {
      index++;
    }

    let buffer = '';
    let escaped = false;
    for (; index < source.length; index++) {
      const ch = source[index];

      if (quote) {
        if (!escaped && ch === '\\') {
          escaped = true;
          buffer += ch;
          continue;
        }

        if (!escaped && ch === quote) {
          break;
        }

        escaped = false;
        buffer += ch;
        continue;
      }

      if (ch === ',' || ch === '}' || ch === '\n' || ch === '\r') {
        break;
      }

      buffer += ch;
    }

    return this.unescapeJsonLikeString(buffer).trim();
  }

  private cleanupJsonArtifacts(value: string, preferredFieldName: string): string {
    if (!value) {
      return '';
    }

    let cleaned = value
      .replace(/```json/gi, '')
      .replace(/```/g, '')
      .trim();

    const escapedField = preferredFieldName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    cleaned = cleaned.replace(new RegExp(`^[\\{\\s]*["']?${escapedField}["']?\\s*:\\s*["']?`, 'i'), '');
    cleaned = cleaned.replace(/^[\{\s]*["']?(message|description|title)["']?\s*:\s*["']?/i, '');
    cleaned = cleaned.replace(/["'\s}]+$/g, '').trim();

    cleaned = cleaned
      .replace(/^Complaint details:\s*/i, '')
      .replace(/^Draft response:\s*/i, '')
      .trim();

    return cleaned;
  }

  private unescapeJsonLikeString(value: string): string {
    return value
      .replace(/\\n/g, '\n')
      .replace(/\\r/g, '\r')
      .replace(/\\t/g, '\t')
      .replace(/\\"/g, '"')
      .replace(/\\'/g, "'")
      .replace(/\\\\/g, '\\');
  }

  private polishSentence(value: string): string {
    const text = (value ?? '').trim();
    if (!text) {
      return '';
    }

    const capitalized = text.charAt(0).toUpperCase() + text.slice(1);
    if (/[.!?]$/.test(capitalized)) {
      return capitalized;
    }

    return `${capitalized}.`;
  }

  private forceImproveText(value: string, fallbackSuffix: string): string {
    const original = (value ?? '').trim();
    if (!original) {
      return '';
    }

    const polished = this.polishSentence(original);
    if (polished !== original) {
      return polished;
    }

    return `${polished} ${fallbackSuffix}`.trim();
  }
}

