import { Component, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { register } from 'swiper/element/bundle';

// Enregistrer Swiper UNE SEULE FOIS au niveau du module
register();
@Component({
  selector: 'app-testimoni',
  templateUrl: './testimoni.component.html',
  styleUrl: './testimoni.component.scss'
})
export class TestimoniComponent {
 @ViewChild('swiperContainer', { static: false }) swiperContainer!: ElementRef;

  listTestimoni = [
    {
      name: "Iezh Robert",
      image: "assets/people-3.png",
      city: "Warsaw",
      country: "Poland",
      rating: "4.5",
      testimoni:
        "Wow... I am very happy to use this VPN, it turned out to be more than my expectations and so far there have been no problems. LaslesVPN always the best",
    },
    {
      name: "Jane Smith",
      image: "assets/people-2.png",
      city: "Berlin",
      country: "Germany",
      rating: "4.5",
      testimoni:
        "Wow... I am very happy to use this VPN, it turned out to be more than my expectations and so far there have been no problems.",
    },
    {
      name: "John Doe",
      image: "assets/people-1.png",
      city: "London",
      country: "UK",
      rating: "4.5",
      testimoni:
        "LaslesVPN always the best VPN service I have ever used.",
    }
  ];

  ngAfterViewInit(): void {
   
    const swiperEl = this.swiperContainer?.nativeElement;
    if (swiperEl) {
      Object.assign(swiperEl, {
        slidesPerView: 3,
        slidesPerGroup: 2,
        spaceBetween: 30,
        pagination: {
          clickable: true,
          dynamicBullets: true,
        },
        navigation: false,
        loop: false,
        speed: 500,
        breakpoints: {
          0: {
            slidesPerView: 1,
            slidesPerGroup: 1,
            spaceBetween: 20,
          },
          480: {
            slidesPerView: 1,
            slidesPerGroup: 1,
            spaceBetween: 20,
          },
          770: {
            slidesPerView: 2,
            slidesPerGroup: 2,
            spaceBetween: 25,
          },
          1024: {
            slidesPerView: 3,
            slidesPerGroup: 2,
            spaceBetween: 30,
          },
        },
      });
      swiperEl.initialize();
    }
  }

  // Méthodes pour contrôler le swiper
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
