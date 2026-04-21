import { Component } from '@angular/core';
import { Router } from '@angular/router';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
 model = { email: '', password: '' };
  showPassword = false;
  loading = false;
  error = '';

  tilt = { rx: 0, ry: 0 };

  constructor(private router: Router) {}

  onTilt(e: MouseEvent) {
    const el = e.currentTarget as HTMLElement;
    const r = el.getBoundingClientRect();
    const x = (e.clientX - r.left) / r.width;   // 0..1
    const y = (e.clientY - r.top) / r.height;   // 0..1

    // rotation douce
    this.tilt.ry = (x - 0.5) * 10;  // -5..5
    this.tilt.rx = -(y - 0.5) * 10; // -5..5
  }

  resetTilt() {
    this.tilt = { rx: 0, ry: 0 };
  }

  async onSubmit() {
    this.error = '';
    this.loading = true;
    try {
      // TODO: auth service
      if (!this.model.email || !this.model.password) throw new Error('Missing credentials');
      this.router.navigate(['/admin']);

    } catch (e: any) {
      
      this.error = e?.message ?? 'Login failed';
    } finally {
      this.loading = false;
    }
  }

  


}


