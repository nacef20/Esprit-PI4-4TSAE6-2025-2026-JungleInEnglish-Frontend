import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';

const routes: Routes = [
  {
    path: '',
    component: BackOfficeComponent,
    children: [
      {
        path: 'planning',
        loadChildren: () => import('./planning/planning.module').then(m => m.PlanningModule)
      },
      {
        path: 'salle',
        loadChildren: () => import('./salle/salle.module').then(m => m.SalleModule)
      },
      {
        path: 'groupe',
        loadChildren: () => import('./groupe/groupe.module').then(m => m.GroupeModule)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BackOfficeRoutingModule { }
