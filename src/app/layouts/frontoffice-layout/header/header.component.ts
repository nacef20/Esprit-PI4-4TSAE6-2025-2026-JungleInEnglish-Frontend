import { Component, HostListener, inject } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  currentUser = this.authService.currentUser;

  isTeacherRole() {
    const user = this.currentUser();
    return user?.roles?.some(r =>
      r.toUpperCase() === 'ROLE_TEACHER' || r.toUpperCase() === 'TEACHER'
    );
  }

  scrollActive = false;
  activeLink = '';

  @HostListener('window:scroll')
  onWindowScroll() {
    this.scrollActive = window.scrollY > 20;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  scrollTo(id: string) {
    const el = document.getElementById(id);
    if (!el) return;
    const header = document.querySelector('header') as HTMLElement | null;
    const offset = (header?.offsetHeight ?? 80) + 12;
    const y = el.getBoundingClientRect().top + window.scrollY - offset;
    window.scrollTo({ top: y, behavior: 'smooth' });
    this.activeLink = id;
  }
}
