import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ReglementRoutingModule } from './reglement-routing.module';
import { ReglementListComponent } from './reglement-list/reglement-list.component';
import { ReglementFormComponent } from './reglement-form/reglement-form.component';

@NgModule({
  declarations: [
    ReglementListComponent,
    ReglementFormComponent
  ],
  imports: [
    CommonModule,
    ReglementRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class ReglementModule { }
