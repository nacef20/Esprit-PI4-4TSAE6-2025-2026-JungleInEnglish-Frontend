import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';


const routes: Routes = [{ path: '', component: BackOfficeComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule, CommonModule, FormsModule]
})
export class BackOfficeRoutingModule {


 


 }
