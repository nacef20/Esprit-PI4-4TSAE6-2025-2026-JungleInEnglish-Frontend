// Résoudre "global is not defined" pour les librairies Node comme stompjs/sockjs
(window as any).global = window;

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import 'swiper/element/bundle';
import { AppModule } from './app/app.module';
import { initKeycloak } from './app/auth/keycloak.config';  

initKeycloak()
  .then(() => {
    platformBrowserDynamic()
      .bootstrapModule(AppModule, {
        ngZoneEventCoalescing: true
      })
      .catch(err => console.error('Bootstrap Error:', err));
  })
  .catch(err => {
    console.error('Keycloak Init Failed:', err);
    // Optionally, show a simple error message on the page instead of remaining blank
    document.body.innerHTML = `
      <div style="padding: 20px; font-family: sans-serif; color: #721c24; background: #f8d7da; border: 1px solid #f5c6cb; border-radius: 4px; margin: 20px;">
        <h2>Keycloak Authentication Failed</h2>
        <p>${err?.message || 'Check if Keycloak server is running on localhost:8085 and CORS is configured.'}</p>
        <button onclick="window.location.reload()">Retry</button>
      </div>
    `;
  });
