import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrollAnimationComponent } from '../scroll-animation/scroll-animation.component';

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [CommonModule, ScrollAnimationComponent],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.css',
})
export class PricingComponent {
  plans = [
    {
      name: 'Free Plan',
      price: 'Free',
      image: 'https://placehold.co/128x128/f7ede2/2f6f67?text=Free',
      features: [
        'Unlimited Bandwidth',
        'Encrypted Connection',
        'No Traffic Logs',
        'Works on All Devices',
      ],
    },
    {
      name: 'Standard Plan',
      price: '$9 / mo',
      image: 'https://placehold.co/128x128/f7ede2/2f6f67?text=Standard',
      features: [
        'Unlimited Bandwidth',
        'Encrypted Connection',
        'No Traffic Logs',
        'Works on All Devices',
        'Connect Anywhere',
      ],
    },
    {
      name: 'Premium Plan',
      price: '$12 / mo',
      image: 'https://placehold.co/128x128/f7ede2/2f6f67?text=Premium',
      features: [
        'Unlimited Bandwidth',
        'Encrypted Connection',
        'No Traffic Logs',
        'Works on All Devices',
        'Connect Anywhere',
        'Get New Features',
      ],
    },
  ];
}
