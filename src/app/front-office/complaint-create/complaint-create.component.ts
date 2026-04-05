import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { ComplaintService } from '../../services/complaint.service';
import { ComplaintStatus } from '../../models/complaint.model';

@Component({
  selector: 'app-complaint-create',
  templateUrl: './complaint-create.component.html',
  styleUrls: ['./complaint-create.component.scss']
})
export class ComplaintCreateComponent implements OnInit {

  complaintForm!: FormGroup;
  submitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private complaintService: ComplaintService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.complaintForm = this.fb.group({
      title: [''],
      description: ['']
    });
  }

  onSubmit(): void {
    this.submitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const complaint = {
      ...this.complaintForm.value,
      status: ComplaintStatus.EN_ATTENTE
    };

    this.complaintService.create(complaint).subscribe({
      next: () => {
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

