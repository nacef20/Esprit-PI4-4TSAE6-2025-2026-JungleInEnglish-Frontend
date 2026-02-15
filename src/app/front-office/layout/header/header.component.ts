import { Component, HostListener } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
   scrollActive = false;
  activeLink: string = '';

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
}
