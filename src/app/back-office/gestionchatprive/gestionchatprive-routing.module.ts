import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GestionchatpriveComponent } from './gestionchatprive.component';
import { JoingroupComponent } from './joingroup/joingroup.component';
const routes: Routes = [
  { path: '', component: GestionchatpriveComponent },
  { path: 'joingroup', component: JoingroupComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GestionchatpriveRoutingModule { 


  

}
