import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrollAnimationComponent } from '../scroll-animation/scroll-animation.component';

@Component({
  selector: 'app-feature',
  standalone: true,
  imports: [CommonModule, ScrollAnimationComponent],
  templateUrl: './feature.component.html',
  styleUrl: './feature.component.css',
})
export class FeatureComponent {
  features: string[] = [
    'Powerful online protection.',
    'Internet without borders.',
    'Supercharged VPN',
    'No specific time limits.',
  ];
}
