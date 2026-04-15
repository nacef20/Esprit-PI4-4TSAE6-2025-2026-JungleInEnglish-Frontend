import { Component } from '@angular/core';

@Component({
  selector: 'app-pricing',
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.scss'
})
export class PricingComponent {
plans = [
    {
      name: 'Free Plan',
      price: 'Free',
      image: 'assets/Free.png',
      features: [
        'Unlimited Bandwitch',
        'Encrypted Connection',
        'No Traffic Logs',
        'Works on All Devices'
      ]
    },
    {
      name: 'Standard Plan',
      price: '$9 / mo',
      image: 'assets/Standard.png',
      features: [
        'Unlimited Bandwitch',
        'Encrypted Connection',
        'No Traffic Logs',
        'Works on All Devices',
        'Connect Anywhere'
      ]
    },
    {
      name: 'Premium Plan',
      price: '$12 / mo',
      image: 'assets/Premium.png',
      features: [
        'Unlimited Bandwitch',
        'Encrypted Connection',
        'No Traffic Logs',
        'Works on All Devices',
        'Connect Anywhere',
        'Get New Features'
      ]
    }
  ];
}
