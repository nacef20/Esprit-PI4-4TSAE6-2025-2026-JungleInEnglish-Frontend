import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { TransfertsImpactComponent } from './transferts-impact/transferts-impact.component';
import { ScoreSanteComponent } from './score-sante/score-sante.component';
import { RecommandationsGroupeComponent } from './recommandations-groupe/recommandations-groupe.component';
import { AlertesAnomaliesComponent } from './alertes-anomalies/alertes-anomalies.component';
import { AuditParcoursComponent } from './audit-parcours/audit-parcours.component';
import { EquilibrageGroupesComponent } from './equilibrage-groupes/equilibrage-groupes.component';
import { AnalysisDashboardComponent } from './analysis-dashboard/analysis-dashboard.component';

const routes: Routes = [
  { path: '', component: AnalysisDashboardComponent },
  { path: 'transferts', component: TransfertsImpactComponent },
  { path: 'sante', component: ScoreSanteComponent },
  { path: 'recommandations', component: RecommandationsGroupeComponent },
  { path: 'alertes', component: AlertesAnomaliesComponent },
  { path: 'audit', component: AuditParcoursComponent },
  { path: 'equilibrage', component: EquilibrageGroupesComponent }
];

@NgModule({
  declarations: [
    TransfertsImpactComponent,
    ScoreSanteComponent,
    RecommandationsGroupeComponent,
    AlertesAnomaliesComponent,
    AuditParcoursComponent,
    EquilibrageGroupesComponent,
    AnalysisDashboardComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class AnalysisModule { }
