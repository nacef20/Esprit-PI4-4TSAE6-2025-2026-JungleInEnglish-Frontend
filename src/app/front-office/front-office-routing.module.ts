import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FrontOfficeComponent } from './front-office.component';
import { LoginComponent } from './login/login.component';
import { FirstloginComponent } from './pages/Reset/firstlogin/firstlogin.component';
import { ForgotpasswordComponent } from './pages/Reset/forgotpassword/forgotpassword.component';

const routes: Routes = [{ path: '', component: FrontOfficeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotpasswordComponent },
  { path: 'first-login', component: FirstloginComponent },
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FrontOfficeRoutingModule { }
