import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-900 via-purple-900 to-black p-4">
      <div class="max-w-md w-full bg-white/10 backdrop-blur-xl rounded-3xl p-8 shadow-2xl border border-white/20">
        <div class="text-center mb-8">
          <h1 class="text-4xl font-bold text-white mb-2">Welcome Back</h1>
          <p class="text-indigo-200">Sign in to your account</p>
        </div>

        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-indigo-200 mb-1">Username</label>
            <input 
              type="text" 
              formControlName="username"
              class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
              placeholder="Your username"
            >
            @if (loginForm.get('username')?.touched && loginForm.get('username')?.invalid) {
              <p class="text-red-400 text-xs mt-1">Username is required</p>
            }
          </div>

          <div>
            <label class="block text-sm font-medium text-indigo-200 mb-1">Password</label>
            <input 
              type="password" 
              formControlName="password"
              class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
              placeholder="••••••••"
            >
            @if (loginForm.get('password')?.touched && loginForm.get('password')?.invalid) {
              <p class="text-red-400 text-xs mt-1">Password is required</p>
            }
          </div>

          @if (errorMessage) {
            <div class="p-3 bg-red-500/20 border border-red-500/50 rounded-xl text-red-200 text-sm">
              {{ errorMessage }}
            </div>
          }

          <button 
            type="submit" 
            [disabled]="loginForm.invalid || isLoading"
            class="w-full py-3 bg-indigo-600 hover:bg-indigo-500 disabled:bg-indigo-800 disabled:cursor-not-allowed text-white font-bold rounded-xl transition-all shadow-lg shadow-indigo-500/25"
          >
            {{ isLoading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>

        <p class="mt-8 text-center text-indigo-200 text-sm">
          Don't have an account? 
          <a routerLink="/signup" class="text-white font-semibold hover:underline">Sign up</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  isLoading = false;
  errorMessage = '';

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          this.isLoading = false;

          if (this.authService.isTeacher()) {
            this.router.navigate(['/admin']);
          } else {
            this.router.navigate(['/todos']);
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Invalid username or password';
          console.error('Login error', err);
        }
      });
    }
  }
}
