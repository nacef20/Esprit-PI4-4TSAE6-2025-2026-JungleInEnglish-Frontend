import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GestionchatpriveComponent } from './gestionchatprive.component';

const routes: Routes = [{ path: '', component: GestionchatpriveComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GestionchatpriveRoutingModule { 


  

}
