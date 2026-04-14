import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth/AuthGuard';

const routes: Routes = [
  // ✅ Front Office — public, sans login
  {
    path: '',
    loadChildren: () => import('./front-office/front-office.module').then(m => m.FrontOfficeModule)
  },
  {
    path: 'front-office',
    loadChildren: () => import('./front-office/front-office.module').then(m => m.FrontOfficeModule)
  },
  {
    path: 'front',
    loadChildren: () => import('./front-office/front-office.module').then(m => m.FrontOfficeModule)
  },

    // courses should also open the FrontOffice module
  {
    path: 'courses',
    loadChildren: () => import('./front-office/front-office.module').then(m => m.FrontOfficeModule)
  },
  // 🔒 Back Office — guard temporairement désactivé pour tests
  {
    path: 'admin',
    // canActivate: [AuthGuard], // ← décommentez pour réactiver Keycloak
    loadChildren: () => import('./back-office/back-office.module').then(m => m.BackOfficeModule)
  },
  {
    path: 'back',
    // canActivate: [AuthGuard], // ← décommentez pour réactiver Keycloak
    loadChildren: () => import('./back-office/back-office.module').then(m => m.BackOfficeModule)
  },

  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}

