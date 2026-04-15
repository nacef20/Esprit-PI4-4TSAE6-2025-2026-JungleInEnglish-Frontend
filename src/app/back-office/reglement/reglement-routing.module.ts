import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReglementListComponent } from './reglement-list/reglement-list.component';
import { ReglementFormComponent } from './reglement-form/reglement-form.component';

const routes: Routes = [
  { path: '', component: ReglementListComponent },
  { path: 'add', component: ReglementFormComponent },
  { path: 'edit/:id', component: ReglementFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReglementRoutingModule { }
