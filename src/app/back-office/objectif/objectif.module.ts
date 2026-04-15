import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ObjectifRoutingModule } from './objectif-routing.module';
import { ObjectifListComponent } from './objectif-list/objectif-list.component';
import { ObjectifFormComponent } from './objectif-form/objectif-form.component';

@NgModule({
  declarations: [
    ObjectifListComponent,
    ObjectifFormComponent
  ],
  imports: [
    CommonModule,
    ObjectifRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class ObjectifModule { }
