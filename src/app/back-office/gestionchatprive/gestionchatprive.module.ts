import { NgModule } from '@angular/core';


import { GestionchatpriveRoutingModule } from './gestionchatprive-routing.module';
import { GestionchatpriveComponent } from './gestionchatprive.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateGroupComponent } from './create-group/create-group.component';

import { JoingroupComponent } from './joingroup/joingroup.component';
import { MembregroupComponent } from './membregroup/membregroup.component';


@NgModule({
  declarations: [
    GestionchatpriveComponent,
    CreateGroupComponent,
    
    JoingroupComponent,
         MembregroupComponent
  ],
  imports: [
    CommonModule,
    GestionchatpriveRoutingModule,FormsModule
  ]
})
export class GestionchatpriveModule {



 }
