import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GroupeListComponent } from './groupe-list/groupe-list.component';
import { GroupeFormComponent } from './groupe-form/groupe-form.component';
import { GroupeDetailComponent } from './groupe-detail/groupe-detail.component';

const routes: Routes = [
  { path: '', component: GroupeListComponent },
  { path: 'details/:id', component: GroupeDetailComponent },
  { path: 'detail/:id', component: GroupeDetailComponent },
  { path: 'create', component: GroupeFormComponent },
  { path: 'edit/:id', component: GroupeFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GroupeRoutingModule {}
