import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: '', renderMode: RenderMode.Prerender },
  { path: 'todos', renderMode: RenderMode.Prerender },
  { path: 'events', renderMode: RenderMode.Prerender },
  { path: 'admin', renderMode: RenderMode.Prerender },
  { path: 'admin/todos', renderMode: RenderMode.Prerender },
  { path: 'admin/events', renderMode: RenderMode.Prerender },
  { path: 'todos/:id', renderMode: RenderMode.Server },
  { path: 'events/:id', renderMode: RenderMode.Server },
  { path: '**', renderMode: RenderMode.Server },
];
