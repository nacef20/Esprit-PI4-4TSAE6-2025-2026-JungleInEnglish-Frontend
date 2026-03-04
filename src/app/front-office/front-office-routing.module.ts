import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FrontOfficeComponent } from './front-office.component';
import { LoginComponent } from './login/login.component';
import { FirstloginComponent } from './pages/Reset/firstlogin/firstlogin.component';
import { ForgotpasswordComponent } from './pages/Reset/forgotpassword/forgotpassword.component';
import { CoursesPageComponent } from '../back-office/pages/courses-page/courses-page.component';

const routes: Routes = [
  { path: '', component: FrontOfficeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotpasswordComponent },
  { path: 'first-login', component: FirstloginComponent },
  { path: 'courses', component: CoursesPageComponent },
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FrontOfficeRoutingModule { }
