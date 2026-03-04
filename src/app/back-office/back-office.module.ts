import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { BackOfficeRoutingModule } from './back-office-routing.module';
import { BackOfficeComponent } from './back-office.component';

import { SidebarComponent } from './back-layout/sidebar/sidebar.component';
import { HeaderComponent } from './back-layout/header/header.component';
import { SalleModule } from './salle/salle.module';



@NgModule({
  declarations: [
    BackOfficeComponent,
    SidebarComponent,
    HeaderComponent,



  ],
  imports: [
    CommonModule,
    BackOfficeRoutingModule,
    FormsModule,
    SalleModule
  ]
})
export class BackOfficeModule { }
