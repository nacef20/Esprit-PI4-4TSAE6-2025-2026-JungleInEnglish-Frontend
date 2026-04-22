import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { FrontOfficeRoutingModule } from './front-office-routing.module';
import { FrontOfficeComponent } from './front-office.component';

import { LoginComponent } from './login/login.component';
import { HeaderComponent } from './layout/header/header.component';
import { FooterComponent } from './layout/footer/footer.component';
import { ScrollAnimationComponent } from './layout/scroll-animation/scroll-animation.component';

import { FeatureComponent } from './pages/feature/feature.component';
import { HeroComponent } from './pages/hero/hero.component';
import { PricingComponent } from './pages/pricing/pricing.component';
import { TestimoniComponent } from './pages/testimoni/testimoni.component';
import { HomeComponent } from './pages/home/home.component';
import { ForgotpasswordComponent } from './pages/Reset/forgotpassword/forgotpassword.component';
import { FirstloginComponent } from './pages/Reset/firstlogin/firstlogin.component';

import { CoursesListComponent } from '../components/courses-list/courses-list.component';
import { LessonsPublicComponent } from './pages/lessons-public/lessons-public.component'; // adjust path
import { CoursesPageComponent } from '../back-office/pages/courses-page/courses-page.component';
import { RouterModule } from '@angular/router';
import { EventGalleryComponent } from './event-gallery/event-gallery.component';
import { MyComplaintsComponent } from './my-complaints/my-complaints.component';
import { ComplaintCreateComponent } from './complaint-create/complaint-create.component';
import { ComplaintViewComponent } from './complaint-view/complaint-view.component';
import { JobBoardComponent } from './pages/job-board/job-board.component';
import { TodoListComponent } from './todo/todo-list/todo-list.component';
import { TodoDashboardComponent } from './todo/todo-dashboard/todo-dashboard.component';
import { NgChartsModule } from 'ng2-charts';


@NgModule({
  declarations: [
    FrontOfficeComponent,  // ← Keep in declarations
    LoginComponent,
    HeaderComponent,
    FooterComponent,
    FeatureComponent,
    ScrollAnimationComponent,
    HeroComponent,
    PricingComponent,
    TestimoniComponent,
    HomeComponent,
    ForgotpasswordComponent,
    FirstloginComponent,
    CoursesListComponent,
    LessonsPublicComponent,
    CoursesPageComponent,
    EventGalleryComponent,
    MyComplaintsComponent,
    ComplaintCreateComponent,
    ComplaintViewComponent,
    JobBoardComponent,
    TodoListComponent,
    TodoDashboardComponent
  ],
  imports: [
    CommonModule,
    FrontOfficeRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule,
    NgChartsModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class FrontOfficeModule { }
