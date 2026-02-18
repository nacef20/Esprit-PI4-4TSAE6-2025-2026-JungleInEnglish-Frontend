import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';

const routes: Routes = [
  {
    path: '',                    
    component: BackOfficeComponent,
    children: [                   // toutes les routes s'affichent DANS le layout
      {
        path: 'gestionchatprive',
        loadChildren: () => import('./gestionchatprive/gestionchatprive.module')
          .then(m => m.GestionchatpriveModule)
      },
      {
        path: '',                  // optionnel : redirection vers chat par défaut
        redirectTo: 'gestionchatprive',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BackOfficeRoutingModule {}
