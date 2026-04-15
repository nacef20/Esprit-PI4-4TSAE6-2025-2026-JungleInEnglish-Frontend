import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { PlanningRoutingModule } from './planning-routing.module';
import { PlanningListComponent } from './planning-list/planning-list.component';
import { PlanningFormComponent } from './planning-form/planning-form.component';
import { PlanningGenerateComponent } from './planning-generate/planning-generate.component';
import { PlanningWeeklyLoadComponent } from './planning-weekly-load/planning-weekly-load.component';
import { PlanningSemesterReplicateComponent } from './planning-semester-replicate/planning-semester-replicate.component';

@NgModule({
    declarations: [
        PlanningListComponent,
        PlanningFormComponent,
        PlanningGenerateComponent,
        PlanningWeeklyLoadComponent,
        PlanningSemesterReplicateComponent
    ],
    imports: [
        CommonModule,
        PlanningRoutingModule,
        FormsModule,
        ReactiveFormsModule
    ]
})
export class PlanningModule { }
