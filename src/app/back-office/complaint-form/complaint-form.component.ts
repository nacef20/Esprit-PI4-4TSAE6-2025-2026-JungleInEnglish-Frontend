import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ComplaintService } from '../../services/complaint.service';
import { ComplaintStatus } from '../../models/complaint.model';

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
      error: () => {
        this.submitting = false;
        this.errorMessage = 'An error occurred. Please try again.';
      }
    });
  }

  cancel(): void {
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
}

