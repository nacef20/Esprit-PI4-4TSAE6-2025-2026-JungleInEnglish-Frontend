import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { GroupeRoutingModule } from './groupe-routing.module';
import { GroupeListComponent } from './groupe-list/groupe-list.component';
import { GroupeFormComponent } from './groupe-form/groupe-form.component';

@NgModule({
  declarations: [
    GroupeListComponent,
    GroupeFormComponent
  ],
  imports: [
    CommonModule,
    GroupeRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class GroupeModule {}
