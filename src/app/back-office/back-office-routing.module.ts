import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';
import { CoursesManagementComponent } from './pages/courses-management/courses-management.component';
import { LessonsManagementComponent } from './pages/lessons-management/lessons-management.component';
import { MyCoursesComponent } from './pages/my-courses/my-courses.component';

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
        path: 'my-courses',
        component: MyCoursesComponent
      },
      {
        path: 'gestionchatprive',
        loadChildren: () =>
          import('./gestionchatprive/gestionchatprive.module')
            .then(m => m.GestionchatpriveModule)
      },
      {
        path: 'planning',
        loadChildren: () =>
          import('./planning/planning.module').then(m => m.PlanningModule)
      },
      {
        path: 'salle',
        loadChildren: () =>
          import('./salle/salle.module').then(m => m.SalleModule)
      },
      {
        path: 'groupe',
        loadChildren: () =>
          import('./groupe/groupe.module').then(m => m.GroupeModule)
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