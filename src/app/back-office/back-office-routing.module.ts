import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';
import { CoursesManagementComponent } from './pages/courses-management/courses-management.component';
import { LessonsManagementComponent } from './pages/lessons-management/lessons-management.component';
import { MyCoursesComponent } from './pages/my-courses/my-courses.component';
import { QuizListComponent } from './quiz-list/quiz-list.component';
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
import { ComplaintListComponent } from './complaint-list/complaint-list.component';
import { ComplaintFormComponent } from './complaint-form/complaint-form.component';
import { ComplaintDetailComponent } from './complaint-detail/complaint-detail.component';
import { ComplaintStatsDashboardComponent } from './complaint-stats-dashboard/complaint-stats-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: BackOfficeComponent,
    children: [
      {
        path: 'courses',
        component: CoursesManagementComponent
      },
      {
        path: 'lessons',
        component: LessonsManagementComponent
      },
      {
        path: 'my-courses',
        component: MyCoursesComponent
      },
      {
        path: 'quizzes',
        component: QuizListComponent
      },
      {
        path: 'quizzes/new',
        component: QuizFormComponent
      },
      {
        path: 'quizzes/edit/:id',
        component: QuizFormComponent
      },
      {
        path: 'attempts',
        component: AttemptListComponent
      },
      {
        path: 'attempts/:attemptId',
        component: AttemptDetailComponent
      },
      {
        path: 'quizzes/ai',
        component: AiQuizGeneratorComponent
      },
      {
        path: 'quizzes/:quizId/questions',
        component: QuestionListComponent
      },
      {
        path: 'quizzes/:quizId/questions/new',
        component: QuestionFormComponent
      },
      {
        path: 'quizzes/:quizId/questions/edit/:questionId',
        component: QuestionFormComponent
      },
      {
        path: 'questions/:questionId/choices',
        component: ChoiceListComponent
      },
      {
        path: 'questions/:questionId/choices/new',
        component: ChoiceFormComponent
      },
      {
        path: 'questions/:questionId/choices/edit/:choiceId',
        component: ChoiceFormComponent
      },

      {
        path: 'student-quizzes',
        component: QuizStudentListComponent
      },
      {
        path: 'student-quizzes/pass/:id',
        component: QuizPassComponent
      },
      {
        path: 'student-attempts',
        component: AttemptHistoryComponent
      },
      {
        path: 'student-attempts/:attemptId',
        component: AttemptDetailStudentComponent
      },
      {
        path: 'complaints',
        component: ComplaintListComponent
      },
      {
        path: 'complaints/stats',
        component: ComplaintStatsDashboardComponent
      },
      {
        path: 'complaints/:id',
        component: ComplaintDetailComponent
      },
      {
        path: 'gestionchatprive',
        loadChildren: () =>
          import('./gestionchatprive/gestionchatprive.module')
            .then(m => m.GestionchatpriveModule)
      },
      {
        path: 'planning',
        loadChildren: () =>
          import('./planning/planning.module').then(m => m.PlanningModule)
      },
      {
        path: 'salle',
        loadChildren: () =>
          import('./salle/salle.module').then(m => m.SalleModule)
      },
      {
        path: 'groupe',
        loadChildren: () =>
          import('./groupe/groupe.module').then(m => m.GroupeModule)
      },
      {
        path: '',
        redirectTo: 'courses',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BackOfficeRoutingModule { }