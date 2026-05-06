import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';
import { CoursesManagementComponent } from './pages/courses-management/courses-management.component';
import { LessonsManagementComponent } from './pages/lessons-management/lessons-management.component';
import { MyCoursesComponent } from './pages/my-courses/my-courses.component';
import { QuizListComponent } from './quiz-list/quiz-list.component';
import { FeedbackAnalyticsComponent } from './pages/feedback-analytics/feedback-analytics.component';
import { QuizFormComponent } from './quiz-form/quiz-form.component';
import { AttemptListComponent } from './attempt-list/attempt-list.component';
import { AttemptDetailComponent } from './attempt-detail/attempt-detail.component';
import { AiQuizGeneratorComponent } from './ai-quiz-generator/ai-quiz-generator.component';
import { QuestionListComponent } from './question-list/question-list.component';
import { QuestionFormComponent } from './question-form/question-form.component';
import { ChoiceListComponent } from './choice-list/choice-list.component';
import { ChoiceFormComponent } from './choice-form/choice-form.component';
import { QuizStudentListComponent } from '../front-office/quiz-student-list/quiz-student-list.component';
import { QuizPassComponent } from '../front-office/quiz-pass/quiz-pass.component';
import { AttemptHistoryComponent } from '../front-office/attempt-history/attempt-history.component';
import { AttemptDetailStudentComponent } from '../front-office/attempt-detail/attempt-detail.component';
import { adminGuard as shopAdminGuard } from '../shop/core/guards/admin.guard';
import { authGuard as shopAuthGuard } from '../shop/core/guards/auth.guard';
import { EventListComponent } from './event/event-list/event-list.component';
import { EventFormComponent } from './event/event-form/event-form.component';
import { QrScannerComponent } from './event/qr-scanner/qr-scanner.component';
import { ComplaintListComponent } from './complaint-list/complaint-list.component';
import { ComplaintDetailComponent } from './complaint-detail/complaint-detail.component';
import { ComplaintFormComponent } from './complaint-form/complaint-form.component';
import { AdminDashboardComponent } from './recruitment/admin-dashboard.component';
import { AdminTodoDashboardComponent } from './todo-management/admin-todo-dashboard/admin-todo-dashboard.component';
import { UserLevelManagementComponent } from './todo-management/user-level-management/user-level-management.component';

const routes: Routes = [
  {
    path: '',
    component: BackOfficeComponent,
    children: [
      { path: 'courses', component: CoursesManagementComponent },
      { path: 'lessons', component: LessonsManagementComponent },
      { path: 'my-courses', component: MyCoursesComponent },
      { path: 'analytics/feedback', component: FeedbackAnalyticsComponent },
      { path: 'quizzes', component: QuizListComponent },
      { path: 'quizzes/new', component: QuizFormComponent },
      { path: 'quizzes/edit/:id', component: QuizFormComponent },
      { path: 'attempts', component: AttemptListComponent },
      { path: 'attempts/:attemptId', component: AttemptDetailComponent },
      { path: 'quizzes/ai', component: AiQuizGeneratorComponent },
      { path: 'quizzes/:quizId/questions', component: QuestionListComponent },
      { path: 'quizzes/:quizId/questions/new', component: QuestionFormComponent },
      { path: 'quizzes/:quizId/questions/edit/:questionId', component: QuestionFormComponent },
      { path: 'questions/:questionId/choices', component: ChoiceListComponent },
      { path: 'questions/:questionId/choices/new', component: ChoiceFormComponent },
      { path: 'questions/:questionId/choices/edit/:choiceId', component: ChoiceFormComponent },
      { path: 'student-quizzes', component: QuizStudentListComponent },
      { path: 'student-quizzes/pass/:id', component: QuizPassComponent },
      { path: 'student-attempts', component: AttemptHistoryComponent },
      { path: 'student-attempts/:attemptId', component: AttemptDetailStudentComponent },
      {
        path: 'gestionchatprive',
        loadChildren: () => import('./gestionchatprive/gestionchatprive.module').then(m => m.GestionchatpriveModule)
      },
      {
        path: 'planning',
        loadChildren: () => import('./planning/planning.module').then(m => m.PlanningModule)
      },
      {
        path: 'salle',
        loadChildren: () => import('./salle/salle.module').then(m => m.SalleModule)
      },
      {
        path: 'groupe',
        loadChildren: () => import('./groupe/groupe.module').then(m => m.GroupeModule)
      },
      {
        path: 'analyse',
        loadChildren: () => import('./analysis/analysis.module').then(m => m.AnalysisModule)
      },
      {
        path: 'reglement',
        loadChildren: () => import('./reglement/reglement.module').then(m => m.ReglementModule)
      },
      {
        path: 'objectif',
        loadChildren: () => import('./objectif/objectif.module').then(m => m.ObjectifModule)
      },
      {
        path: 'affectations',
        loadChildren: () => import('./affectations/affectation.module').then(m => m.AffectationModule)
      },

       { path: 'gestionforum', loadChildren: () => import('./gestionforum/gestionforum.module').then(m => m.GestionforumModule) },
      {
        path: 'shop',
        canActivate: [shopAuthGuard, shopAdminGuard],
        children: [
          { path: '', loadComponent: () => import('../shop/features/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
          { path: 'products', loadComponent: () => import('../shop/features/admin/admin-product-list/admin-product-list.component').then(m => m.AdminProductListComponent) },
          { path: 'products/create', loadComponent: () => import('../shop/features/products/product-form/product-form.component').then(m => m.ProductFormComponent) },
          { path: 'products/:id/edit', loadComponent: () => import('../shop/features/products/product-form/product-form.component').then(m => m.ProductFormComponent) },
          { path: 'orders', loadComponent: () => import('../shop/features/orders/order-admin/order-admin.component').then(m => m.OrderAdminComponent) },
          { path: 'inventory', loadComponent: () => import('../shop/features/admin/inventory-dashboard/inventory-dashboard.component').then(m => m.InventoryDashboardComponent) },
          { path: 'categories', loadComponent: () => import('../shop/features/categories/category-list/category-list.component').then(m => m.CategoryListComponent) },
          { path: 'categories/create', loadComponent: () => import('../shop/features/categories/category-form/category-form.component').then(m => m.CategoryFormComponent) },
          { path: 'categories/:id/edit', loadComponent: () => import('../shop/features/categories/category-form/category-form.component').then(m => m.CategoryFormComponent) },
          { path: 'users', loadComponent: () => import('../shop/features/users/user-list/user-list.component').then(m => m.UserListComponent) },
          { path: 'users/create', loadComponent: () => import('../shop/features/users/user-form/user-form.component').then(m => m.UserFormComponent) },
          { path: 'users/:id/edit', loadComponent: () => import('../shop/features/users/user-form/user-form.component').then(m => m.UserFormComponent) },
          
        ]
      },
      { path: 'events', component: EventListComponent },
      { path: 'events/new', component: EventFormComponent },
      { path: 'events/edit/:id', component: EventFormComponent },
      { path: 'events/scan', component: QrScannerComponent },
      { path: 'complaints', component: ComplaintListComponent },
      { path: 'complaints/new', component: ComplaintFormComponent },
      { path: 'complaints/edit/:id', component: ComplaintFormComponent },
      {
        path: 'complaints/stats',
        loadComponent: () =>
          import('./complaint-stats-dashboard/complaint-stats-dashboard.component').then(
            m => m.ComplaintStatsDashboardComponent
          )
      },
      { path: 'complaints/:id', component: ComplaintDetailComponent },
      { path: 'recruitment/:jobId', component: AdminDashboardComponent },
      { path: 'recruitment', component: AdminDashboardComponent },
      { path: 'todo-stats', component: AdminTodoDashboardComponent },
      { path: 'student-levels', component: UserLevelManagementComponent },
      { path: '', redirectTo: 'courses', pathMatch: 'full' }
    ]
  },

 
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BackOfficeRoutingModule { }