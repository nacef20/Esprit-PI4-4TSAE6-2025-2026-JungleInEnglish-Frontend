import { Component } from '@angular/core';
import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  // Remove 'imports' array - that's only for standalone components
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  constructor(private authService: AuthService) {}

ngOnInit() {
  this.authService.getMe().subscribe({
    next: (res) => console.log('ME:', res),
    error: (err) => console.error('ME ERROR:', err)
  });
}
  title = 'school-management';
}