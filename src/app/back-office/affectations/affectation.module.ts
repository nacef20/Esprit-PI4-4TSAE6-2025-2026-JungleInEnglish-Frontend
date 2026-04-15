import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { GestionMembreGroupeComponent } from './gestion-membre-groupe/gestion-membre-groupe.component';
import { ProfilGroupeEtudiantComponent } from './profil-groupe-etudiant/profil-groupe-etudiant.component';
import { DashboardAffectationsComponent } from './dashboard-affectations/dashboard-affectations.component';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardAffectationsComponent },
  { path: 'etudiants/:etudiantId/groupe', component: ProfilGroupeEtudiantComponent }
];

@NgModule({
  declarations: [
    GestionMembreGroupeComponent,
    ProfilGroupeEtudiantComponent,
    DashboardAffectationsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ],
  exports: [
    GestionMembreGroupeComponent
  ]
})
export class AffectationModule { }