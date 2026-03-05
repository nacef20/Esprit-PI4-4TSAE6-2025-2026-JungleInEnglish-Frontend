import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { rootRedirectGuard } from './core/guards/root-redirect.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent
      ),
  },
  {
    path: 'signup',
    loadComponent: () =>
      import('./features/auth/signup/signup.component').then(
        (m) => m.SignupComponent
      ),
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [rootRedirectGuard],
    loadComponent: () =>
      import('./features/landing/landing/landing.component').then(
        (m) => m.LandingComponent
      ),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layouts/frontoffice-layout/frontoffice-layout.component').then(
        (m) => m.FrontofficeLayoutComponent
      ),
    children: [
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full',
      },
      {
        path: 'home',
        loadComponent: () =>
          import('./features/landing/landing/landing.component').then(
            (m) => m.LandingComponent
          ),
      },
      {
        path: 'todos',
        loadComponent: () =>
          import(
            './features/todos/components/todo-list/todo-list.component'
          ).then((m) => m.TodoListComponent),
      },
      {
        path: 'todos/:id',
        loadComponent: () =>
          import(
            './features/todos/components/todo-detail/todo-detail.component'
          ).then((m) => m.TodoDetailComponent),
      },
      {
        path: 'todos/stats',
        loadComponent: () =>
          import(
            './features/admin/todo-stats/todo-stats.component'
          ).then((m) => m.TodoStatsComponent),
      },
      {
        path: 'events',
        loadComponent: () =>
          import(
            './features/events/components/event-list/event-list.component'
          ).then((m) => m.EventListComponent),
      },
      {
        path: 'events/:id',
        loadComponent: () =>
          import(
            './features/events/components/event-detail/event-detail.component'
          ).then((m) => m.EventDetailComponent),
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { role: 'ROLE_TEACHER' },
    loadComponent: () =>
      import('./layouts/backoffice-layout/backoffice-layout.component').then(
        (m) => m.BackofficeLayoutComponent
      ),
    children: [
      {
        path: '',
        redirectTo: 'todos',
        pathMatch: 'full',
      },
      {
        path: 'todos',
        loadComponent: () =>
          import(
            './features/admin/todos-admin/todos-admin.component'
          ).then((m) => m.TodosAdminComponent),
      },
      {
        path: 'todos/stats',
        loadComponent: () =>
          import(
            './features/admin/todo-stats/todo-stats.component'
          ).then((m) => m.TodoStatsComponent),
      },
      {
        path: 'events',
        loadComponent: () =>
          import(
            './features/admin/events-admin/events-admin.component'
          ).then((m) => m.EventsAdminComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
