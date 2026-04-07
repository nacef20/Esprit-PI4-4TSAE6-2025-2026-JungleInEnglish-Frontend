
import { AfterViewInit, Component, ElementRef, Input, Renderer2 } from '@angular/core';
@Component({
  selector: 'app-scroll-animation',
  templateUrl: './scroll-animation.component.html',
  styleUrl: './scroll-animation.component.scss'
})
export class ScrollAnimationComponent {
 @Input() className: string = '';
  @Input() animationClass: string = 'animate-fadeInUp';

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngAfterViewInit(): void {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          this.renderer.addClass(this.el.nativeElement, this.animationClass);
          observer.unobserve(this.el.nativeElement);
        }
      },
      { threshold: 0.8 }
    );

    observer.observe(this.el.nativeElement);
  }
}
