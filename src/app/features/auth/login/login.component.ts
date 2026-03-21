import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <h1>Connexion</h1>
        
        @if (environment.mockMode) {
          <div class="mock-alert bg-yellow-100 p-3 mb-4 rounded text-sm text-yellow-800">
            <strong>Mode Simulation Actif</strong><br>
            Aucun serveur Keycloak requis. Choisissez un rôle pour simuler la connexion.
          </div>
          
          <form [formGroup]="mockForm" (ngSubmit)="onMockLogin()">
            <div class="form-group mb-3">
              <label>Email simulé</label>
              <input type="email" class="form-control" formControlName="email">
            </div>
            <div class="form-group mb-4">
              <label>Rôle</label>
              <select class="form-control" formControlName="role">
                <option value="user">Utilisateur (Acheteur)</option>
                <option value="admin">Administrateur</option>
              </select>
            </div>
            <button type="submit" class="btn btn-primary w-100 p-2">Simuler la connexion</button>
          </form>
        } @else {
          <div class="text-center p-4">
            <p class="mb-4">Vous allez être redirigé vers le portail de connexion sécurisé.</p>
            <button (click)="onKeycloakLogin()" class="btn btn-primary w-100 p-2">Continuer vers la connexion</button>
          </div>
        }
      </div>
    </div>
  `,
  styles: `
    .auth-container { display: flex; justify-content: center; padding: 4rem 1rem; }
    .auth-card { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
    h1 { margin-bottom: 1.5rem; text-align: center; font-size: 1.5rem; }
  `
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly environment = environment;

  readonly mockForm = this.fb.group({
    email: ['mock@example.com', Validators.required],
    role: ['user', Validators.required]
  });

  onMockLogin() {
    if (this.mockForm.valid) {
      this.authService.login(this.mockForm.getRawValue() as { email: string; role: string });
    }
  }

  onKeycloakLogin() {
    this.authService.login();
  }
}
