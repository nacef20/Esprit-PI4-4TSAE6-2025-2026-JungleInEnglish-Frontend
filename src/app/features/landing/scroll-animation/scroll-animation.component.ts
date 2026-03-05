import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  Renderer2,
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-scroll-animation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './scroll-animation.component.html',
  styleUrl: './scroll-animation.component.css',
})
export class ScrollAnimationComponent implements AfterViewInit {
  @Input() className = '';
  @Input() animationClass = 'animate-fade-in';

  constructor(
    private el: ElementRef,
    private renderer: Renderer2
  ) {}

  ngAfterViewInit(): void {
    if (typeof IntersectionObserver === 'undefined') {
      this.renderer.addClass(this.el.nativeElement, this.animationClass);
      return;
    }
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          this.renderer.addClass(
            this.el.nativeElement,
            this.animationClass
          );
          observer.unobserve(this.el.nativeElement);
        }
      },
      { threshold: 0.2 }
    );
    observer.observe(this.el.nativeElement);
  }
}
