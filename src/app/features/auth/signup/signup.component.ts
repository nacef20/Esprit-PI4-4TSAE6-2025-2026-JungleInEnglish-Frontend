import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-900 via-purple-900 to-black p-4">
      <div class="max-w-md w-full bg-white/10 backdrop-blur-xl rounded-3xl p-8 shadow-2xl border border-white/20">
        <div class="text-center mb-8">
          <h1 class="text-4xl font-bold text-white mb-2">Create Account</h1>
          <p class="text-indigo-200">Join our community today</p>
        </div>

        <form [formGroup]="signupForm" (ngSubmit)="onSubmit()" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-indigo-200 mb-1">Username</label>
            <input 
              type="text" 
              formControlName="username"
              class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-light"
              placeholder="johndoe"
            >
          </div>

          <div>
            <label class="block text-sm font-medium text-indigo-200 mb-1">Email</label>
            <input 
              type="email" 
              formControlName="email"
              class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-light"
              placeholder="john@example.com"
            >
          </div>

          <div>
            <label class="block text-sm font-medium text-indigo-200 mb-1">Password</label>
            <input 
              type="password" 
              formControlName="password"
              class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-light"
              placeholder="••••••••"
            >
          </div>

          <div>
            <label class="block text-sm font-medium text-indigo-200 mb-1">Role</label>
            <div class="grid grid-cols-2 gap-4">
              <button 
                type="button"
                (click)="signupForm.patchValue({ roles: ['student'] })"
                [class]="signupForm.get('roles')?.value?.includes('student') ? 'bg-indigo-600 border-indigo-400' : 'bg-white/5 border-white/10'"
                class="py-2 px-4 rounded-xl border text-white transition-all text-sm"
              >
                Student
              </button>
              <button 
                type="button"
                (click)="signupForm.patchValue({ roles: ['teacher'] })"
                [class]="signupForm.get('roles')?.value?.includes('teacher') ? 'bg-indigo-600 border-indigo-400' : 'bg-white/5 border-white/10'"
                class="py-2 px-4 rounded-xl border text-white transition-all text-sm"
              >
                Teacher
              </button>
            </div>
          </div>

          @if (errorMessage) {
            <div class="p-3 bg-red-500/20 border border-red-500/50 rounded-xl text-red-200 text-sm">
              {{ errorMessage }}
            </div>
          }

          <button 
            type="submit" 
            [disabled]="signupForm.invalid || isLoading"
            class="w-full py-3 bg-indigo-600 hover:bg-indigo-500 disabled:bg-indigo-800 disabled:cursor-not-allowed text-white font-bold rounded-xl transition-all shadow-lg shadow-indigo-500/25 mt-4"
          >
            {{ isLoading ? 'Creating account...' : 'Sign Up' }}
          </button>
        </form>

        <p class="mt-8 text-center text-indigo-200 text-sm">
          Already have an account? 
          <a routerLink="/login" class="text-white font-semibold hover:underline">Sign in</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
  `]
})
export class SignupComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  signupForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    roles: [['student'], Validators.required]
  });

  isLoading = false;
  errorMessage = '';

  onSubmit() {
    if (this.signupForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      this.authService.signup(this.signupForm.value).subscribe({
        next: () => {
          this.isLoading = false;
          this.router.navigate(['/login'], { queryParams: { registered: true } });
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Error occurred during signup';
          console.error('Signup error', err);
        }
      });
    }
  }
}
