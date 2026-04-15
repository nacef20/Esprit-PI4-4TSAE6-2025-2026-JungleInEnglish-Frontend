import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PlanningListComponent } from './planning-list/planning-list.component';
import { PlanningFormComponent } from './planning-form/planning-form.component';
import { PlanningGenerateComponent } from './planning-generate/planning-generate.component';
import { PlanningWeeklyLoadComponent } from './planning-weekly-load/planning-weekly-load.component';
import { PlanningSemesterReplicateComponent } from './planning-semester-replicate/planning-semester-replicate.component';

const routes: Routes = [
    { path: '', component: PlanningListComponent },
    { path: 'create', component: PlanningFormComponent },
    { path: 'edit/:id', component: PlanningFormComponent },
    { path: 'generate', component: PlanningGenerateComponent },
    { path: 'weekly-load', component: PlanningWeeklyLoadComponent },
    { path: 'semester-replicate', component: PlanningSemesterReplicateComponent }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PlanningRoutingModule { }
