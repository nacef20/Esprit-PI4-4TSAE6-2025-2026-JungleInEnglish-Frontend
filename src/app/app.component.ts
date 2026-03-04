import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  // Remove 'imports' array - that's only for standalone components
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'school-management';
}