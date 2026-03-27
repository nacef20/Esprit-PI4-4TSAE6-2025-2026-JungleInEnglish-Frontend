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

/* platformBrowserDynamic().bootstrapModule(AppModule, {
  ngZoneEventCoalescing: true
})
  .catch(err => console.error(err)); */