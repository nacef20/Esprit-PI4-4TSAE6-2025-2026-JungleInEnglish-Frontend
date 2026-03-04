import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';
import { CoursesManagementComponent } from './pages/courses-management/courses-management.component';
import { LessonsManagementComponent } from './pages/lessons-management/lessons-management.component';
import { QuizListComponent } from './quiz-list/quiz-list.component';
import { QuizFormComponent } from './quiz-form/quiz-form.component';
import { AttemptListComponent } from './attempt-list/attempt-list.component';
import { AttemptDetailComponent } from './attempt-detail/attempt-detail.component';
import { AiQuizGeneratorComponent } from './ai-quiz-generator/ai-quiz-generator.component';
import { QuestionListComponent } from './question-list/question-list.component';
import { QuestionFormComponent } from './question-form/question-form.component';
import { ChoiceListComponent } from './choice-list/choice-list.component';
import { ChoiceFormComponent } from './choice-form/choice-form.component';

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
        path: 'gestionchatprive',
        loadChildren: () =>
          import('./gestionchatprive/gestionchatprive.module')
            .then(m => m.GestionchatpriveModule)
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