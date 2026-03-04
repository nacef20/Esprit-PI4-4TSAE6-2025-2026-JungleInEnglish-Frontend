import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FrontOfficeComponent } from './front-office.component';
import { LoginComponent } from './login/login.component';
import { FirstloginComponent } from './pages/Reset/firstlogin/firstlogin.component';
import { ForgotpasswordComponent } from './pages/Reset/forgotpassword/forgotpassword.component';
import { CoursesPageComponent } from '../back-office/pages/courses-page/courses-page.component';
import { QuizStudentListComponent } from './quiz-student-list/quiz-student-list.component';
import { QuizPassComponent } from './quiz-pass/quiz-pass.component';
import { AttemptHistoryComponent } from './attempt-history/attempt-history.component';
import { AttemptDetailStudentComponent } from './attempt-detail/attempt-detail.component';

const routes: Routes = [
  { path: '', component: FrontOfficeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotpasswordComponent },
  { path: 'first-login', component: FirstloginComponent },
  { path: 'courses', component: CoursesPageComponent },
  { path: 'quizzes', component: QuizStudentListComponent },
  { path: 'quiz/:id', component: QuizPassComponent },
  { path: 'my-attempts', component: AttemptHistoryComponent },
  { path: 'my-attempts/:attemptId', component: AttemptDetailStudentComponent },
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FrontOfficeRoutingModule { }
