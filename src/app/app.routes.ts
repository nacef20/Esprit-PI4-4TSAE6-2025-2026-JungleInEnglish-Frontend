import { Routes } from '@angular/router';
import { LoginComponent } from './component/login.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { SignupComponent } from './component/signup.component';
import { AdminDashboardComponent } from './component/admin-dashboard.component';
import { AuthGuard } from './service/auth.guard';
import { AdminGuard } from './service/admin.guard';
import { UserManagementComponent } from './component/user-management.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [AdminGuard] },
  { path: 'admin/notifications', loadComponent: () => import('./component/notifications/notifications.component').then(m => m.NotificationsComponent), canActivate: [AdminGuard] },
  { path: 'admin/job-offers/:jobId', component: AdminDashboardComponent, canActivate: [AdminGuard] },
  { path: 'users', component: UserManagementComponent, canActivate: [AdminGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];
