

import { Component, OnInit, AfterViewInit, HostListener } from '@angular/core';
@Component({
  selector: 'app-front-office',
  templateUrl: './front-office.component.html',
  styleUrl: './front-office.component.scss'
})
export class FrontOfficeComponent {



  ngAfterViewInit(): void {
    this.setupParallaxEffect();
    this.setupScrollAnimations();
    this.setup3DCardEffects();
  }

  // Effet parallax au scroll
  @HostListener('window:scroll', [])
  onWindowScroll() {
    const scrolled = window.pageYOffset;
    
    // Parallax layers
    const layer1 = document.querySelector('.parallax-layer-1') as HTMLElement;
    const layer2 = document.querySelector('.parallax-layer-2') as HTMLElement;
    
    if (layer1) {
      layer1.style.transform = `translateZ(-50px) scale(1.05) translateY(${scrolled * 0.3}px)`;
    }
    
    if (layer2) {
      layer2.style.transform = `translateZ(0) translateY(${scrolled * 0.1}px)`;
    }
  }

  private setupParallaxEffect(): void {
    // Configuration initiale du parallax
    console.log('Parallax effect initialized');
  }

  private setupScrollAnimations(): void {
    const sections = document.querySelectorAll('section');
    
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('animate-in');
        }
      });
    }, {
      threshold: 0.1,
      rootMargin: '0px 0px -100px 0px'
    });

    sections.forEach(section => {
      observer.observe(section);
    });
  }

  private setup3DCardEffects(): void {
    // Effet 3D au survol des cartes
    const cards = document.querySelectorAll('.feature-card, .pricing-card');
    
    cards.forEach(card => {
      card.addEventListener('mousemove', (e: Event) => {
        const mouseEvent = e as MouseEvent;
        const rect = (card as HTMLElement).getBoundingClientRect();
        const x = mouseEvent.clientX - rect.left;
        const y = mouseEvent.clientY - rect.top;
        
        const centerX = rect.width / 2;
        const centerY = rect.height / 2;
        
        const rotateX = (y - centerY) / 10;
        const rotateY = (centerX - x) / 10;
        
        (card as HTMLElement).style.transform = `
          perspective(1000px)
          rotateX(${rotateX}deg)
          rotateY(${rotateY}deg)
          translateZ(10px)
        `;
      });
      
      card.addEventListener('mouseleave', () => {
        (card as HTMLElement).style.transform = '';
      });
    });
  }
}
