import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CategoryService } from '../../../core/services/category.service';
import { MessageService } from '../../../core/services/message.service';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>{{ isEdit() ? 'Modifier la catégorie' : 'Nouvelle catégorie' }}</h1>
      </div>
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-card">
        <div class="form-group">
          <label for="name">Nom</label>
          <input id="name" class="form-control" formControlName="name" placeholder="Nom de la catégorie" />
          @if (form.get('name')?.invalid && form.get('name')?.touched) {
            <span class="form-error">Le nom est requis</span>
          }
        </div>
        <div class="form-group">
          <label for="imageName">Image</label>
          <input id="imageName" class="form-control" formControlName="imageName" placeholder="default.jpg" />
        </div>
        <div class="form-group">
          <label>
            <input type="checkbox" formControlName="isActive" /> Actif
          </label>
        </div>
        <div class="flex gap-2 mt-3">
          <button type="submit" class="btn btn-primary" [disabled]="form.invalid">{{ isEdit() ? 'Mettre à jour' : 'Créer' }}</button>
          <button type="button" class="btn btn-secondary" (click)="goBack()">Annuler</button>
        </div>
      </form>
    </div>
  `,
  styles: `
    :host { display: block; padding-bottom: 3rem; }
    .form-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 2rem; max-width: 500px; }
  `,
})
export class CategoryFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly catService = inject(CategoryService);
  private readonly msg = inject(MessageService);

  readonly isEdit = signal(false);
  private editId = 0;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    imageName: ['default.jpg'],
    isActive: [true],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.editId = +id;
      this.catService.getById(this.editId).subscribe(c => this.form.patchValue(c));
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const data = this.form.getRawValue();
    const obs = this.isEdit()
      ? this.catService.update(this.editId, data)
      : this.catService.create(data);
    obs.subscribe({
      next: () => { this.msg.success(this.isEdit() ? 'Catégorie mise à jour' : 'Catégorie créée'); this.router.navigate(['/categories']); },
    });
  }

  goBack(): void { this.router.navigate(['/categories']); }
}
