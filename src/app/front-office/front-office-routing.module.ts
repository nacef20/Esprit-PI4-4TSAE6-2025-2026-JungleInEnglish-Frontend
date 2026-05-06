import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FrontOfficeComponent } from './front-office.component';
import { LoginComponent } from './login/login.component';
import { FirstloginComponent } from './pages/Reset/firstlogin/firstlogin.component';
import { ForgotpasswordComponent } from './pages/Reset/forgotpassword/forgotpassword.component';
import { CoursesPageComponent } from '../back-office/pages/courses-page/courses-page.component';
import { EventGalleryComponent } from './event-gallery/event-gallery.component';
import { MyComplaintsComponent } from './my-complaints/my-complaints.component';
import { ComplaintCreateComponent } from './complaint-create/complaint-create.component';
import { ComplaintViewComponent } from './complaint-view/complaint-view.component';
import { JobBoardComponent } from './pages/job-board/job-board.component';
import { TodoListComponent } from './todo/todo-list/todo-list.component';
import { TodoDashboardComponent } from './todo/todo-dashboard/todo-dashboard.component';

const routes: Routes = [
  { path: '', component: FrontOfficeComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotpasswordComponent },
  { path: 'first-login', component: FirstloginComponent },
  { path: 'courses', component: CoursesPageComponent },
  { path: 'events', component: EventGalleryComponent },
  { path: 'my-complaints', component: MyComplaintsComponent },
  { path: 'my-complaints/new', component: ComplaintCreateComponent },
  { path: 'my-complaints/:id', component: ComplaintViewComponent },
  { path: 'jobs', component: JobBoardComponent },
  { path: 'my-todos', component: TodoListComponent },
  { path: 'my-stats', component: TodoDashboardComponent },
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FrontOfficeRoutingModule { }
