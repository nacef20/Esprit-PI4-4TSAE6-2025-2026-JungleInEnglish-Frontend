import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GestionforumComponent } from './gestionforum.component';

const routes: Routes = [{ path: '', component: GestionforumComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GestionforumRoutingModule { }
