import { Component } from '@angular/core';

@Component({
  selector: 'app-feature',
  templateUrl: './feature.component.html',
  styleUrl: './feature.component.scss'
})
export class FeatureComponent {
  features: string[] = [
    "Powerfull online protection.",
    "Internet without borders.",
    "Supercharged VPN",
    "No specific time limits."
  ];
}
