import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CategoryService } from '../../../services/category.service';
import { MessageService } from '../../../services/message.service';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>{{ isEdit() ? 'Edit Category' : 'New Category' }}</h1>
      </div>
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-card">
        <div class="form-group">
          <label for="name">Name</label>
          <input id="name" class="form-control" formControlName="name" placeholder="Category name" />
          @if (form.get('name')?.invalid && form.get('name')?.touched) {
            <span class="form-error">Name is required</span>
          }
        </div>
        <div class="form-group">
          <label for="imageName">Icon</label>
          <select id="imageName" class="form-control" formControlName="imageName">
            <option value="📚">📚 Courses / Training</option>
            <option value="📖">📖 Reading / Books</option>
            <option value="📝">📝 Exercises</option>
            <option value="🎓">🎓 Diploma / Certification</option>
            <option value="🗣️">🗣️ Speaking</option>
            <option value="🎧">🎧 Listening</option>
            <option value="✍️">✍️ Writing</option>
            <option value="🧠">🧠 Logic / Memory</option>
            <option value="✏️">✏️ Grammar</option>
            <option value="🔤">🔤 Vocabulary</option>
            <option value="🏫">🏫 Classes / Levels</option>
            <option value="🌐">🌐 Modern Languages</option>
          </select>
        </div>
        <div class="form-group">
          <label>
            <input type="checkbox" formControlName="isActive" /> Active
          </label>
        </div>
        <div class="flex gap-2 mt-3">
          <button type="submit" class="btn btn-primary" [disabled]="form.invalid">{{ isEdit() ? 'Update' : 'Create' }}</button>
          <button type="button" class="btn btn-secondary" (click)="goBack()">Cancel</button>
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
    imageName: ['📚'],
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
      next: () => { 
        this.msg.success(this.isEdit() ? 'Category updated' : 'Category created'); 
        this.router.navigate([this.getRedirectPath()]); 
      },
      error: (err) => {
        // Display the specific backend validation message if available (409 Conflict etc)
        const backendMsg = err?.error?.message || err?.error || null;
        if (typeof backendMsg === 'string' && backendMsg.length > 0) {
          this.msg.error(backendMsg);
        } else {
          this.msg.error('An error occurred while saving');
        }
        console.error(err);
      }
    });
  }

  private getRedirectPath(): string {
    return this.router.url.includes('/admin/shop') ? '/admin/shop/categories' : '/shop/categories';
  }

  goBack(): void { this.router.navigate([this.getRedirectPath()]); }
}
