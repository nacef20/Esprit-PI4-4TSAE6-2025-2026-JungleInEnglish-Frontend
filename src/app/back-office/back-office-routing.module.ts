import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BackOfficeComponent } from './back-office.component';
import { QuizListComponent } from './quiz-list/quiz-list.component';
import { QuizFormComponent } from './quiz-form/quiz-form.component';
import { QuestionListComponent } from './question-list/question-list.component';
import { QuestionFormComponent } from './question-form/question-form.component';
import { ChoiceListComponent } from './choice-list/choice-list.component';
import { ChoiceFormComponent } from './choice-form/choice-form.component';
import { AttemptListComponent } from './attempt-list/attempt-list.component';
import { AttemptDetailComponent } from './attempt-detail/attempt-detail.component';

const routes: Routes = [
  {
    path: '',                    
    component: BackOfficeComponent,
    children: [
      {
        path: 'gestionchatprive',
        loadChildren: () => import('./gestionchatprive/gestionchatprive.module')
          .then(m => m.GestionchatpriveModule)
      },
      // Quiz CRUD
      { path: 'quizzes', component: QuizListComponent },
      { path: 'quizzes/new', component: QuizFormComponent },
      { path: 'quizzes/edit/:id', component: QuizFormComponent },
      // Question CRUD
      { path: 'quizzes/:quizId/questions', component: QuestionListComponent },
      { path: 'quizzes/:quizId/questions/new', component: QuestionFormComponent },
      { path: 'quizzes/:quizId/questions/edit/:questionId', component: QuestionFormComponent },
      // Choice CRUD
      { path: 'questions/:questionId/choices', component: ChoiceListComponent },
      { path: 'questions/:questionId/choices/new', component: ChoiceFormComponent },
      { path: 'questions/:questionId/choices/edit/:choiceId', component: ChoiceFormComponent },
      // Attempts
      { path: 'attempts', component: AttemptListComponent },
      { path: 'attempts/:attemptId', component: AttemptDetailComponent },
      {
        path: '',
        redirectTo: 'quizzes',
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BackOfficeRoutingModule {}
