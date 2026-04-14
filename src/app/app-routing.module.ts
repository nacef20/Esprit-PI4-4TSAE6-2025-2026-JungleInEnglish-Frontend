import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth/AuthGuard';

const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('./front-office/front-office.module').then(m => m.FrontOfficeModule)
  },
  {
    path: 'front-office',
    loadChildren: () =>
      import('./front-office/front-office.module').then(m => m.FrontOfficeModule)
  },
  {
    path: 'front',
    loadChildren: () =>
      import('./front-office/front-office.module').then(m => m.FrontOfficeModule)
  },
  {
    path: 'courses',
    loadChildren: () =>
      import('./front-office/front-office.module').then(m => m.FrontOfficeModule)
  },
  {
    path: 'admin',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./back-office/back-office.module').then(m => m.BackOfficeModule)
  },
  {
    path: 'back',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./back-office/back-office.module').then(m => m.BackOfficeModule)
  },
  {
    path: 'shop',
    loadChildren: () =>
      import('./shop/shop.routes').then(m => m.SHOP_ROUTES)
  }
  // [DIAGNOSTIC] - Temporarily disabled for debugging routing failures
  /*
  {
    path: '**',
    redirectTo: ''
  }
  */
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    scrollPositionRestoration: 'enabled',
    enableTracing: true // Added tracing for deep diagnostics
  })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
