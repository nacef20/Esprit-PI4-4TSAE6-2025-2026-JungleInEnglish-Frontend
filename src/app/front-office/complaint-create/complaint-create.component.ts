import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
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
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  onSubmit(): void {
    if (this.complaintForm.invalid) {
      this.complaintForm.markAllAsTouched();
      return;
    }

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
      error: () => {
        this.submitting = false;
        this.errorMessage = 'An error occurred. Please try again.';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/my-complaints']);
  }
}

