import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
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
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      status: [ComplaintStatus.EN_ATTENTE, Validators.required]
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
          this.errorMessage = 'Réclamation introuvable.';
        }
      });
    }
  }

  onSubmit(): void {
    if (this.complaintForm.invalid) {
      this.complaintForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const complaint = this.complaintForm.value;
    const request = this.isEdit && this.complaintId
      ? this.complaintService.update(this.complaintId, complaint)
      : this.complaintService.create(complaint);

    request.subscribe({
      next: () => {
        this.successMessage = this.isEdit ? 'Réclamation modifiée avec succès !' : 'Réclamation créée avec succès !';
        setTimeout(() => this.router.navigate(['/admin/complaints']), 1500);
      },
      error: () => {
        this.submitting = false;
        this.errorMessage = 'Une erreur est survenue. Veuillez réessayer.';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/complaints']);
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'En attente';
      case 'TRAITEE': return 'Traitée';
      case 'REJETEE': return 'Rejetée';
      default: return status;
    }
  }
}
