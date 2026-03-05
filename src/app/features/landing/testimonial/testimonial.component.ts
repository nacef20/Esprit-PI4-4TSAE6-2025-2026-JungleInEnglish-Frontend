import {
  Component,
  ViewChild,
  ElementRef,
  AfterViewInit,
  CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core';
import { register } from 'swiper/element/bundle';
import { CommonModule } from '@angular/common';

register();

@Component({
  selector: 'app-testimonial',
  standalone: true,
  imports: [CommonModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  templateUrl: './testimonial.component.html',
  styleUrl: './testimonial.component.css',
})
export class TestimonialComponent implements AfterViewInit {
  @ViewChild('swiperContainer', { static: false }) swiperContainer!: ElementRef;

  listTestimoni = [
    {
      name: 'Iezh Robert',
      image: 'https://placehold.co/96x96/0ea37a/fff?text=IR',
      city: 'Warsaw',
      country: 'Poland',
      rating: '4.5',
      testimoni:
        'Wow... I am very happy to use this service, it turned out to be more than my expectations and so far there have been no problems.',
    },
    {
      name: 'Jane Smith',
      image: 'https://placehold.co/96x96/0b6b56/fff?text=JS',
      city: 'Berlin',
      country: 'Germany',
      rating: '4.5',
      testimoni:
        'Wow... I am very happy to use this service, it turned out to be more than my expectations.',
    },
    {
      name: 'John Doe',
      image: 'https://placehold.co/96x96/2f6f67/fff?text=JD',
      city: 'London',
      country: 'UK',
      rating: '4.5',
      testimoni: 'Always the best service I have ever used.',
    },
  ];

  ngAfterViewInit(): void {
    if (typeof window === 'undefined') return;
    const swiperEl = this.swiperContainer?.nativeElement;
    if (swiperEl) {
      Object.assign(swiperEl, {
        slidesPerView: 3,
        slidesPerGroup: 2,
        spaceBetween: 30,
        pagination: { clickable: true, dynamicBullets: true },
        navigation: false,
        loop: false,
        speed: 500,
        breakpoints: {
          0: { slidesPerView: 1, slidesPerGroup: 1, spaceBetween: 20 },
          480: { slidesPerView: 1, slidesPerGroup: 1, spaceBetween: 20 },
          770: { slidesPerView: 2, slidesPerGroup: 2, spaceBetween: 25 },
          1024: { slidesPerView: 3, slidesPerGroup: 2, spaceBetween: 30 },
        },
      });
      swiperEl.initialize();
    }
  }

  goToPrevSlide(): void {
    const swiperEl = this.swiperContainer?.nativeElement;
    if (swiperEl?.swiper) {
      swiperEl.swiper.slidePrev();
    }
  }

  goToNextSlide(): void {
    const swiperEl = this.swiperContainer?.nativeElement;
    if (swiperEl?.swiper) {
      swiperEl.swiper.slideNext();
    }
  }
}
