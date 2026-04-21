import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { GroupeRoutingModule } from './groupe-routing.module';
import { GroupeListComponent } from './groupe-list/groupe-list.component';
import { GroupeFormComponent } from './groupe-form/groupe-form.component';
import { GroupeDetailComponent } from './groupe-detail/groupe-detail.component';
import { AffectationModule } from '../affectations/affectation.module';

@NgModule({
  declarations: [
    GroupeListComponent,
    GroupeFormComponent,
    GroupeDetailComponent
  ],
  imports: [
    CommonModule,
    GroupeRoutingModule,
    AffectationModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class GroupeModule {}
