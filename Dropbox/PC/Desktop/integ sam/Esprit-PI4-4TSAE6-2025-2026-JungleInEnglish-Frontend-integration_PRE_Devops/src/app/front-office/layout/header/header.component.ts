import { Component, HostListener } from '@angular/core';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  scrollActive = false;
  activeLink: string = '';

  constructor(private authService: AuthService) {}

  @HostListener('window:scroll')
  onWindowScroll() {
    this.scrollActive = window.scrollY > 20;
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

  login() {
    // Explicitly target the admin area upon successful login
    this.authService.login(window.location.origin + '/admin');
  }

  logout() {
    this.authService.logout();
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }
}
