import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ObjectifListComponent } from './objectif-list/objectif-list.component';
import { ObjectifFormComponent } from './objectif-form/objectif-form.component';

const routes: Routes = [
  { path: '', component: ObjectifListComponent },
  { path: 'add', component: ObjectifFormComponent },
  { path: 'edit/:id', component: ObjectifFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ObjectifRoutingModule { }
