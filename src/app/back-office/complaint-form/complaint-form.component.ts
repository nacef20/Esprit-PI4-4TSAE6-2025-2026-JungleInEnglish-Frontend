import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ComplaintService } from '../../services/complaint.service';
import { ComplaintStatus } from '../../models/complaint.model';
import { ComplaintAiAssistMode } from '../../models/complaint-ai-assist.model';

interface ParsedAiPayload {
  title?: string;
  description?: string;
}

const ALLOWED_ATTACHMENT_EXTENSIONS = ['png', 'jpg', 'jpeg', 'pdf'];
const ALLOWED_ATTACHMENT_MIME_TYPES = ['image/png', 'image/jpeg', 'application/pdf'];
const MAX_ATTACHMENT_SIZE = 5 * 1024 * 1024;

@Component({
  selector: 'app-complaint-form',
  templateUrl: './complaint-form.component.html',
  styleUrls: ['./complaint-form.component.scss']
})
export class ComplaintFormComponent implements OnInit {

  complaintForm!: FormGroup;
  isEdit = false;
  complaintId?: number;
  statuses = Object.values(ComplaintStatus);
  submitting = false;
  aiLoading = false;
  successMessage = '';
  errorMessage = '';
  attachmentError = '';
  selectedAttachment: File | null = null;
  removeAttachment = false;
  currentAttachmentFileName = '';

  constructor(
    private fb: FormBuilder,
    private complaintService: ComplaintService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.complaintForm = this.fb.group({
      title: [''],
      description: [''],
      status: [ComplaintStatus.EN_ATTENTE]
    });

    const id = this.route.snapshot.params['id'];
    if (id) {
      this.complaintId = +id;
      this.isEdit = true;
      this.complaintService.getById(this.complaintId).subscribe({
        next: (complaint) => {
          this.complaintForm.patchValue(complaint);
          this.currentAttachmentFileName = complaint.attachmentFileName ?? '';
        },
        error: () => {
          this.errorMessage = 'Complaint not found.';
        }
      });
    }
  }

  onSubmit(): void {
    this.submitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const complaint = this.complaintForm.value;
    const request = this.isEdit && this.complaintId
      ? this.complaintService.update(this.complaintId, complaint, this.selectedAttachment, this.removeAttachment)
      : this.complaintService.create(complaint, this.selectedAttachment);

    request.subscribe({
      next: () => {
        this.successMessage = this.isEdit ? 'Complaint updated successfully!' : 'Complaint created successfully!';
        setTimeout(() => this.router.navigate(['/admin/complaints']), 1500);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = this.getBackendErrorMessage(error, 'An error occurred. Please try again.');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/complaints']);
  }

  onAttachmentSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    this.attachmentError = '';
    if (!file) {
      this.selectedAttachment = null;
      return;
    }

    const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
    const mimeType = (file.type ?? '').toLowerCase();
    const extensionAllowed = ALLOWED_ATTACHMENT_EXTENSIONS.includes(extension);
    const mimeAllowed = !mimeType || ALLOWED_ATTACHMENT_MIME_TYPES.includes(mimeType);

    if (!extensionAllowed || !mimeAllowed) {
      this.selectedAttachment = null;
      this.attachmentError = 'Only PNG, JPG/JPEG, and PDF files are allowed.';
      input.value = '';
      return;
    }

    if (file.size > MAX_ATTACHMENT_SIZE) {
      this.selectedAttachment = null;
      this.attachmentError = 'File size must be 5 MB or less.';
      input.value = '';
      return;
    }

    this.selectedAttachment = file;
    this.removeAttachment = false;
  }

  clearSelectedAttachment(): void {
    this.selectedAttachment = null;
    this.attachmentError = '';
  }

  markCurrentAttachmentForRemoval(): void {
    this.removeAttachment = true;
    this.currentAttachmentFileName = '';
  }

  undoRemoveCurrentAttachment(): void {
    this.removeAttachment = false;
  }

  downloadCurrentAttachment(): void {
    if (!this.complaintId) {
      return;
    }

    this.complaintService.downloadAttachment(this.complaintId).subscribe({
      next: (response) => {
        const fallbackName = this.currentAttachmentFileName || 'complaint-attachment';
        this.triggerDownloadFromResponse(response, fallbackName);
      },
      error: (error) => {
        this.errorMessage = this.getBackendErrorMessage(error, 'Unable to download attachment.');
      }
    });
  }

  hasCurrentAttachment(): boolean {
    return this.isEdit && !!this.currentAttachmentFileName && !this.removeAttachment;
  }

  generateText(): void {
    this.runAiAssist('GENERATE');
  }

  improveDescription(): void {
    this.runAiAssist('IMPROVE');
  }

  private runAiAssist(mode: ComplaintAiAssistMode): void {
    if (this.aiLoading || this.submitting) {
      return;
    }

    this.aiLoading = true;
    this.errorMessage = '';

    const currentTitle = (this.complaintForm.get('title')?.value ?? '').trim();
    const currentDescription = (this.complaintForm.get('description')?.value ?? '').trim();
    const effectiveMode: ComplaintAiAssistMode = mode === 'IMPROVE' && !currentDescription ? 'GENERATE' : mode;

    const payload = {
      title: currentTitle,
      description: currentDescription,
      mode: effectiveMode
    };

    this.complaintService.assistTextWithAi(payload).subscribe({
      next: (result) => {
        const normalized = this.normalizeAiResult(result.suggestedTitle, result.suggestedDescription);
        let nextDescription = (normalized.description || '').trim();

        if (!nextDescription && effectiveMode === 'GENERATE') {
          nextDescription = this.buildLocalGeneratedDescription(currentTitle, currentDescription);
        }

        if (!nextDescription) {
          nextDescription = currentDescription;
        }

        if (effectiveMode === 'IMPROVE' && nextDescription === currentDescription) {
          nextDescription = this.forceImproveText(currentDescription, 'Please review and process this complaint as soon as possible.');
        }

        this.complaintForm.patchValue({
          title: normalized.title,
          description: nextDescription
        });
        this.aiLoading = false;
      },
      error: (error) => {
        this.aiLoading = false;
        this.errorMessage = this.getBackendErrorMessage(
          error,
          'AI assistance is unavailable right now. Please try again.'
        );
      }
    });
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'Pending';
      case 'TRAITEE': return 'Resolved';
      case 'REJETEE': return 'Rejected';
      default: return status;
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

  private normalizeAiResult(suggestedTitle: string, suggestedDescription: string): { title: string; description: string } {
    let title = (suggestedTitle ?? '').trim();
    let description = (suggestedDescription ?? '').trim();

    const parsedFromDescription = this.tryParseAiJson(description);
    const parsedFromTitle = this.tryParseAiJson(title);
    const parsed = parsedFromDescription ?? parsedFromTitle;

    if (parsed) {
      title = (parsed.title ?? title).trim();
      description = (parsed.description ?? description).trim();
    } else {
      const lenientTitle = this.extractJsonFieldLenient(description, 'title') || this.extractJsonFieldLenient(title, 'title');
      const lenientDescription = this.extractJsonFieldLenient(description, 'description')
        || this.extractJsonFieldLenient(title, 'description')
        || this.extractJsonFieldLenient(description, 'message')
        || this.extractJsonFieldLenient(title, 'message');

      if (lenientTitle) {
        title = lenientTitle;
      }

      if (lenientDescription) {
        description = lenientDescription;
      }
    }

    title = this.cleanupJsonArtifacts(title, 'title');
    description = this.cleanupJsonArtifacts(description, 'description');

    return { title, description };
  }

  private tryParseAiJson(value: string): ParsedAiPayload | null {
    if (!value) {
      return null;
    }

    const trimmed = value.trim();
    const start = trimmed.indexOf('{');
    const end = trimmed.lastIndexOf('}');

    if (start < 0 || end <= start) {
      return null;
    }

    const jsonCandidate = trimmed.slice(start, end + 1);

    try {
      const parsed = JSON.parse(jsonCandidate);
      if (!parsed || typeof parsed !== 'object') {
        return null;
      }

      return {
        title: typeof parsed.title === 'string' ? parsed.title : undefined,
        description: typeof parsed.description === 'string' ? parsed.description : undefined
      };
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

  private buildLocalGeneratedDescription(title: string, existingDescription: string): string {
    if (existingDescription) {
      return this.polishSentence(existingDescription);
    }

    if (title) {
      return `A complaint has been submitted regarding "${title}". Please investigate the issue and provide an appropriate resolution.`;
    }

    return 'A complaint has been submitted. Please investigate the issue and provide an appropriate resolution.';
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

