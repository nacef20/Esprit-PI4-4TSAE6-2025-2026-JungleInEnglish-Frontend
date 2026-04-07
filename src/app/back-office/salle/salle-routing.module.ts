import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SalleListComponent } from './salle-list.component';
import { SalleFormComponent } from './salle-form.component';

const routes: Routes = [
  { path: '', component: SalleListComponent },
  { path: 'add', component: SalleFormComponent },
  { path: 'edit/:id', component: SalleFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SalleRoutingModule {}
