import { NgModule, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ServiceWorkerModule } from '@angular/service-worker';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { JwtInterceptor } from './shop/core/interceptors/jwt.interceptor';
import { ErrorInterceptor } from './shop/core/interceptors/error.interceptor';
import { MessageService } from './shop/services/message.service';
import { MessageToastComponent } from './shop/shared/components/message-toast/message-toast.component';

import { AuthInterceptor } from './auth/auth.interceptor';
import { FeedbackModule } from './features/lessons/feedback.module';
@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    FeedbackModule,
    MessageToastComponent,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000'
    })
  ],
  providers: [    {
   provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
   ],
  bootstrap: [AppComponent]
})
export class AppModule { }
