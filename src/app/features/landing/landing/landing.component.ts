import { Component, AfterViewInit, HostListener } from '@angular/core';
import { HomeComponent } from '../home/home.component';
import { FeatureComponent } from '../feature/feature.component';
import { PricingComponent } from '../pricing/pricing.component';
import { TestimonialComponent } from '../testimonial/testimonial.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [HomeComponent, FeatureComponent, PricingComponent, TestimonialComponent],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css',
})
export class LandingComponent implements AfterViewInit {
  @HostListener('window:scroll', [])
  onWindowScroll() {
    if (typeof document === 'undefined') return;
    const scrolled = window.pageYOffset;
    const layer1 = document.querySelector('.parallax-layer-1') as HTMLElement;
    const layer2 = document.querySelector('.parallax-layer-2') as HTMLElement;
    if (layer1) {
      layer1.style.transform = `translateY(${scrolled * 0.3}px)`;
    }
    if (layer2) {
      layer2.style.transform = `translateY(${scrolled * 0.1}px)`;
    }
  }

  ngAfterViewInit(): void {
    if (typeof document !== 'undefined' && typeof IntersectionObserver !== 'undefined') {
      this.setupScrollAnimations();
    }
  }

  private setupScrollAnimations(): void {
    const sections = document.querySelectorAll('section');
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('animate-in');
          }
        });
      },
      { threshold: 0.1, rootMargin: '0px 0px -100px 0px' }
    );
    sections.forEach((section) => observer.observe(section));
  }
}
