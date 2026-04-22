import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule} from '@angular/forms';
import { GestionforumRoutingModule } from './gestionforum-routing.module';
import { GestionforumComponent } from './gestionforum.component';
import { CreatePostComponent } from './create-post/create-post.component';

import { FormsModule } from '@angular/forms';
@NgModule({
  declarations: [
    GestionforumComponent,
    CreatePostComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
     FormsModule,
    GestionforumRoutingModule
  ]
})
export class GestionforumModule { }
