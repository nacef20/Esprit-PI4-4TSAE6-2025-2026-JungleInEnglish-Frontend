import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';
import { CoursesManagementComponent } from './pages/courses-management/courses-management.component';
import { LessonsManagementComponent } from './pages/lessons-management/lessons-management.component';

const routes: Routes = [
  {
    path: '',
    component: BackOfficeComponent,
    children: [
      {
        path: 'courses',
        component: CoursesManagementComponent
      },
      {
        path: 'lessons',
        component: LessonsManagementComponent
      },

      {
        path: 'gestionchatprive',
        loadChildren: () =>
          import('./gestionchatprive/gestionchatprive.module')
            .then(m => m.GestionchatpriveModule)
      },

      {
        path: '',
        redirectTo: 'courses',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BackOfficeRoutingModule { }