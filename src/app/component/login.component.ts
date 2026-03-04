import { Component, OnInit } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule]
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';
  error: string | null = null;
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    // If user is already authenticated, redirect to appropriate dashboard
    if (this.authService.isAuthenticated()) {
      const user = this.authService.getCurrentUser();
      const role = user.role?.toUpperCase();
      if (role === 'ADMIN' || role === 'EMPLOYER') {
        this.router.navigate(['/admin']);
      } else {
        this.router.navigate(['/dashboard']);
      }
    }
  }

  login() {
    this.error = null;
    this.loading = true;
    
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.loading = false;
        const user = this.authService.getCurrentUser();
        console.log('Logged in user:', user);
        const role = user.role?.toUpperCase();
        console.log('User role:', role);
        if (role === 'ADMIN' || role === 'EMPLOYER') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.loading = false;
        // Use friendly message from service, or fallback for auth failure
        const errorMessage = (err && err.message) || (err && typeof err.error === 'string' && err.error) || 'Invalid email or password. Please try again.';
        this.error = errorMessage;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}