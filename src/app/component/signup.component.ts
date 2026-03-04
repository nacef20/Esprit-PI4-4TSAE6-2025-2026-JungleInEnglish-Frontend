import { Component } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule]
})
export class SignupComponent {
  name = '';
  email = '';
  password = '';
  error: string | null = null;
  success: string | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  signup() {
    this.authService.register({ name: this.name, email: this.email, password: this.password }).subscribe({
      next: () => {
        this.success = 'Signup successful! You can now log in.';
        this.error = null;
      },
      error: (err) => {
        // Extract error message from the response
        let errorMessage = 'Signup failed';
        if (err.error && typeof err.error === 'string') {
          errorMessage = err.error;
        } else if (err.message) {
          errorMessage = err.message;
        }
        this.error = errorMessage;
        this.success = null;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
} 