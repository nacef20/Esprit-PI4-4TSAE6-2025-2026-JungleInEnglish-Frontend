import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ComplaintService } from '../../services/complaint.service';
import { ComplaintStatus } from '../../models/complaint.model';
import { ComplaintAiAssistMode } from '../../models/complaint-ai-assist.model';

interface ParsedAiPayload {
  title?: string;
  description?: string;
}

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
      ? this.complaintService.update(this.complaintId, complaint)
      : this.complaintService.create(complaint);

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

    const payload = {
      title: this.complaintForm.get('title')?.value ?? '',
      description: this.complaintForm.get('description')?.value ?? '',
      mode
    };

    this.complaintService.assistTextWithAi(payload).subscribe({
      next: (result) => {
        const normalized = this.normalizeAiResult(result.suggestedTitle, result.suggestedDescription);
        this.complaintForm.patchValue({
          title: normalized.title,
          description: normalized.description
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
    }

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
}

