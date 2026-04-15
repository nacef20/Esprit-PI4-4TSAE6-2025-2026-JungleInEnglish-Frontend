// Résoudre "global is not defined" pour les librairies Node comme stompjs/sockjs
(window as any).global = window;

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import 'swiper/element/bundle';
import { AppModule } from './app/app.module';
import { initKeycloak } from './app/auth/keycloak.config';  

initKeycloak().then(() => {
  platformBrowserDynamic()
    .bootstrapModule(AppModule, {
      ngZoneEventCoalescing: true
    })
    .catch(err => console.error(err));
});
