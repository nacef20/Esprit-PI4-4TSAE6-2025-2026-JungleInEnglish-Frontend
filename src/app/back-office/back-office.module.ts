import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { BackOfficeRoutingModule } from './back-office-routing.module';
import { BackOfficeComponent } from './back-office.component';

import { SidebarComponent } from './back-layout/sidebar/sidebar.component';
import { HeaderComponent } from './back-layout/header/header.component';
import { CoursesManagementComponent } from './pages/courses-management/courses-management.component';
import { FormsModule } from '@angular/forms';
import { LessonsManagementComponent } from './pages/lessons-management/lessons-management.component';
import { LessonsListComponent } from '../components/lessons-list/lessons-list.component';



@NgModule({
  declarations: [
    BackOfficeComponent,
    SidebarComponent,
    HeaderComponent,
    CoursesManagementComponent,
    LessonsManagementComponent,
    LessonsListComponent  
    

  ],
  imports: [
    CommonModule,
    FormsModule,  
    RouterModule,
    BackOfficeRoutingModule,
   
    
  ]
})
export class BackOfficeModule { }
