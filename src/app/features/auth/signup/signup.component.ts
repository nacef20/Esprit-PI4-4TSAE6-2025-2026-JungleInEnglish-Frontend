import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from '../../../core/services/message.service';

function passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password && confirm && password !== confirm ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css',
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly message = inject(MessageService);
  private readonly router = inject(Router);

  readonly form: FormGroup = this.fb.nonNullable.group(
    {
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatchValidator }
  );
  readonly submitting = signal(false);

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) return;
    const { confirmPassword, ...data } = this.form.getRawValue();
    this.submitting.set(true);
    this.auth.signup(data).subscribe({
      next: () => {
        this.message.success('Compte créé. Connectez-vous pour continuer.');
        this.router.navigate(['/login']);
        this.submitting.set(false);
      },
      error: () => this.submitting.set(false),
    });
  }
}
