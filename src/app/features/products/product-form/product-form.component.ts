import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { MessageService } from '../../../core/services/message.service';
import type { Category } from '../../../shared/models/category.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>{{ isEdit() ? 'Modifier le produit' : 'Nouveau produit' }}</h1>
      </div>
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-card">
        <div class="form-row">
          <div class="form-group">
            <label for="title">Titre</label>
            <input id="title" class="form-control" formControlName="title" placeholder="Nom du produit" />
            @if (form.get('title')?.invalid && form.get('title')?.touched) {
              <span class="form-error">Le titre est requis</span>
            }
          </div>
          <div class="form-group">
            <label for="category">Catégorie</label>
            <select id="category" class="form-control" formControlName="category">
              <option value="">Sélectionner...</option>
              @for (cat of categories(); track cat.id) {
                <option [value]="cat.name">{{ cat.name }}</option>
              }
            </select>
          </div>
        </div>
        <div class="form-group">
          <label for="description">Description</label>
          <textarea id="description" class="form-control" formControlName="description" rows="4"></textarea>
        </div>
        <div class="form-group">
          <label for="image">Image</label>
          <input id="image" type="file" class="form-control" (change)="onFileSelected($event)" accept="image/png,image/jpeg,image/jpg,image/webp" />
          @if (!isEdit() && !selectedFile()) {
            <span class="text-xs text-slate-400 mt-1 block">Veuillez sélectionner une image (PNG, JPG, WEBP)</span>
          }
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="price">Prix (TND)</label>
            <input id="price" type="number" class="form-control" formControlName="price" />
          </div>
          <div class="form-group">
            <label for="stock">Stock</label>
            <input id="stock" type="number" class="form-control" formControlName="stock" />
          </div>
          <div class="form-group">
            <label for="discount">Remise (%)</label>
            <input id="discount" type="number" class="form-control" formControlName="discount" min="0" max="100" />
          </div>
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
    .form-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 2rem; max-width: 700px; }
    .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; }
  `,
})
export class ProductFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly msg = inject(MessageService);

  readonly isEdit = signal(false);
  readonly categories = signal<Category[]>([]);
  readonly selectedFile = signal<File | null>(null);
  private editId = 0;

  readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: [''],
    category: [''],
    price: [0, [Validators.required, Validators.min(0)]],
    stock: [0, Validators.min(0)],
    discount: [0, [Validators.min(0), Validators.max(100)]],
    isActive: [true],
  });

  ngOnInit(): void {
    this.categoryService.getActive().subscribe({
      next: (c) => {
        this.categories.set(c);
        if (c.length === 0) {
          console.warn('Aucune catégorie active trouvée');
        }
      },
      error: (err) => {
        console.error('Erreur lors de la récupération des catégories:', err);
        this.msg.error('Impossible de charger les catégories');
      }
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.editId = +id;
      this.productService.getById(this.editId).subscribe({
        next: (p) => this.form.patchValue(p),
        error: (err) => {
          console.error('Erreur lors de la récupération du produit:', err);
          this.msg.error('Impossible de charger les données du produit');
        }
      });
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'];
      if (!allowedTypes.includes(file.type)) {
        this.msg.error('Format de fichier non supporté. Utilisez PNG, JPG ou WEBP.');
        input.value = '';
        return;
      }
      this.selectedFile.set(file);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    if (!this.isEdit() && !this.selectedFile()) {
      this.msg.error('L\'image est requise pour un nouveau produit');
      return;
    }

    const data = this.form.getRawValue();
    const obs = this.isEdit()
      ? this.productService.update(this.editId, data, this.selectedFile() || undefined)
      : this.productService.create(data, this.selectedFile()!);

    obs.subscribe({
      next: () => {
        this.msg.success(this.isEdit() ? 'Produit mis à jour' : 'Produit créé');
        this.router.navigate(['/products']);
      },
      error: (err) => {
        this.msg.error('Une erreur est survenue lors de l\'enregistrement');
        console.error(err);
      }
    });
  }

  goBack(): void { this.router.navigate(['/products']); }
}
