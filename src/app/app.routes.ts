import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layouts/frontoffice-layout/frontoffice-layout.component').then(
        (m) => m.FrontofficeLayoutComponent
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
