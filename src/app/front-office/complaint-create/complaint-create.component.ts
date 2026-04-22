import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
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
  selector: 'app-complaint-create',
  templateUrl: './complaint-create.component.html',
  styleUrls: ['./complaint-create.component.scss']
})
export class ComplaintCreateComponent implements OnInit {

  complaintForm!: FormGroup;
  submitting = false;
  aiLoading = false;
  successMessage = '';
  errorMessage = '';
  attachmentError = '';
  selectedAttachment: File | null = null;

  constructor(
    private fb: FormBuilder,
    private complaintService: ComplaintService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.complaintForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(5000)]]
    });
  }

  onSubmit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.complaintForm.invalid) {
      this.complaintForm.markAllAsTouched();
      this.errorMessage = 'Please provide a valid title (3-255 chars) and description (10-5000 chars).';
      return;
    }

    this.submitting = true;

    const title = (this.complaintForm.get('title')?.value ?? '').trim();
    const description = (this.complaintForm.get('description')?.value ?? '').trim();

    const complaint = {
      title,
      description,
      status: ComplaintStatus.EN_ATTENTE
    };

    this.complaintService.create(complaint, this.selectedAttachment).subscribe({
      next: () => {
        this.submitting = false;
        this.successMessage = 'Complaint submitted successfully!';
        setTimeout(() => this.router.navigate(['/my-complaints']), 1500);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = this.getBackendErrorMessage(error, 'An error occurred. Please try again.');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/my-complaints']);
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
  }

  clearAttachment(): void {
    this.selectedAttachment = null;
    this.attachmentError = '';
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
        let nextTitle = (normalized.title || currentTitle).trim();
        let nextDescription = (normalized.description || '').trim();

        if (!nextTitle && effectiveMode === 'GENERATE') {
          nextTitle = 'Issue encountered while using the platform';
        }

        if (!nextDescription && effectiveMode === 'GENERATE') {
          nextDescription = this.buildLocalGeneratedDescription(nextTitle || currentTitle, currentDescription);
        }

        if (!nextDescription) {
          nextDescription = currentDescription;
        }

        if (effectiveMode === 'IMPROVE' && nextDescription === currentDescription) {
          nextDescription = this.forceImproveText(currentDescription, 'We kindly ask for your assistance to resolve this issue.');
        }

        this.complaintForm.patchValue({
          title: nextTitle || currentTitle,
          description: nextDescription
        });
        this.aiLoading = false;
      },
      error: () => {
        const fallbackTitle = currentTitle || 'Issue encountered while using the platform';
        let fallbackDescription = currentDescription;

        if (effectiveMode === 'GENERATE') {
          fallbackDescription = this.buildLocalGeneratedDescription(fallbackTitle, currentDescription);
        } else if (effectiveMode === 'IMPROVE') {
          fallbackDescription = this.forceImproveText(
            currentDescription || this.buildLocalGeneratedDescription(fallbackTitle, ''),
            'Please review and process this complaint as soon as possible.'
          );
        }

        this.complaintForm.patchValue({
          title: fallbackTitle,
          description: fallbackDescription
        });

        this.aiLoading = false;
        this.successMessage = 'AI service is currently unavailable. A local draft has been generated.';
      }
    });
  }

  private getBackendErrorMessage(error: any, fallback: string): string {
    if (error?.status === 0) {
      return 'Cannot reach complaint service. Verify API Gateway is running on http://localhost:8222.';
    }

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

  get titleError(): string {
    const control = this.complaintForm?.get('title');
    if (!control || !(control.touched || control.dirty) || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'Title is required.';
    }
    if (control.errors['minlength']) {
      return 'Title must contain at least 3 characters.';
    }
    if (control.errors['maxlength']) {
      return 'Title must not exceed 255 characters.';
    }

    return '';
  }

  get descriptionError(): string {
    const control = this.complaintForm?.get('description');
    if (!control || !(control.touched || control.dirty) || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'Description is required.';
    }
    if (control.errors['minlength']) {
      return 'Description must contain at least 10 characters.';
    }
    if (control.errors['maxlength']) {
      return 'Description must not exceed 5000 characters.';
    }

    return '';
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
      return `I am experiencing an issue related to "${title}". Please review my request and help resolve this problem as soon as possible.`;
    }

    return 'I am experiencing an issue and need assistance. Please review my complaint and help me resolve it.';
  }
}
