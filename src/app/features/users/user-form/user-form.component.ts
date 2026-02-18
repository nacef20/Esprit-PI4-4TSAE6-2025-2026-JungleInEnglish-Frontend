import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { MessageService } from '../../../core/services/message.service';
import type { UserCreateDto } from '../../../shared/models/user.model';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css',
})
export class UserFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly messageService = inject(MessageService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form: FormGroup = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  readonly isEdit = computed(() => this.editId() !== null);
  readonly editId = signal<number | null>(null);
  readonly loading = signal(true);
  readonly submitting = signal(false);

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const numId = Number(id);
      if (!Number.isNaN(numId)) {
        this.editId.set(numId);
        // Remove password validation in edit mode
        this.form.get('password')?.clearValidators();
        this.form.get('password')?.updateValueAndValidity();
        this.loadUser(numId);
      } else {
        this.loading.set(false);
      }
    } else {
      this.loading.set(false);
    }
  }

  private loadUser(id: number): void {
    this.userService.getById(id).subscribe({
      next: (user) => {
        this.form.patchValue({ name: user.name, email: user.email });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/users']);
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) return;
    const value = this.form.getRawValue() as UserCreateDto;
    const id = this.editId();

    this.submitting.set(true);
    const request = id
      ? this.userService.update(id, value)
      : this.userService.create(value);

    request.subscribe({
      next: () => {
        this.messageService.success(id ? 'Utilisateur mis à jour.' : 'Utilisateur créé.');
        this.router.navigate(['/users']);
        this.submitting.set(false);
      },
      error: () => this.submitting.set(false),
    });
  }
}
