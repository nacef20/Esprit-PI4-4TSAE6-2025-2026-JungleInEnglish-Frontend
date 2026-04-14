import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SalleListComponent } from './salle-list.component';
import { SalleFormComponent } from './salle-form.component';
import { SalleRoutingModule } from './salle-routing.module';

@NgModule({
  declarations: [SalleListComponent, SalleFormComponent],
  imports: [CommonModule, FormsModule, SalleRoutingModule],
})
export class SalleModule {}
