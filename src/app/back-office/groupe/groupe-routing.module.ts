import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GroupeListComponent } from './groupe-list/groupe-list.component';
import { GroupeFormComponent } from './groupe-form/groupe-form.component';

const routes: Routes = [
  { path: '', component: GroupeListComponent },
  { path: 'create', component: GroupeFormComponent },
  { path: 'edit/:id', component: GroupeFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GroupeRoutingModule {}
