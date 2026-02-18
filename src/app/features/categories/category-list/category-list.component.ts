import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoryService } from '../../../core/services/category.service';
import { MessageService } from '../../../core/services/message.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import type { Category } from '../../../shared/models/category.model';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [RouterLink, PaginationComponent, ConfirmDialogComponent],
  template: `
    <div class="container">
      <div class="page-header flex-between">
        <div>
          <h1>📂 Catégories</h1>
          <p>Gérer les catégories de produits</p>
        </div>
        <a routerLink="/categories/create" class="btn btn-primary">+ Nouvelle catégorie</a>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        <div class="table-wrap">
          <table>
            <thead>
              <tr><th>#</th><th>Nom</th><th>Image</th><th>Statut</th><th>Actions</th></tr>
            </thead>
            <tbody>
              @for (cat of categories(); track cat.id) {
                <tr>
                  <td>{{ cat.id }}</td>
                  <td><strong>{{ cat.name }}</strong></td>
                  <td>{{ cat.imageName }}</td>
                  <td><span class="badge" [class.badge-success]="cat.isActive" [class.badge-danger]="!cat.isActive">{{ cat.isActive ? 'Actif' : 'Inactif' }}</span></td>
                  <td class="flex gap-1">
                    <a [routerLink]="['/categories', cat.id, 'edit']" class="btn btn-sm btn-secondary">✏️</a>
                    <button class="btn btn-sm btn-danger" (click)="confirmDelete(cat)">🗑️</button>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="5" class="text-center text-muted" style="padding:2rem;">Aucune catégorie</td></tr>
              }
            </tbody>
          </table>
        </div>
        <app-pagination [pageNo]="pageNo()" [totalPages]="totalPages()" [totalElements]="totalElements()" [first]="first()" [last]="last()" (pageChange)="onPageChange($event)" />
      }

      @if (deletingCat(); as cat) {
        <app-confirm-dialog title="Supprimer la catégorie" [message]="'Supprimer « ' + cat.name + ' » ?'" (confirm)="doDelete(cat)" (cancel)="deletingCat.set(null)" />
      }
    </div>
  `,
  styles: `:host { display: block; padding-bottom: 3rem; }`,
})
export class CategoryListComponent implements OnInit {
  private readonly catService = inject(CategoryService);
  private readonly msg = inject(MessageService);

  readonly categories = signal<Category[]>([]);
  readonly loading = signal(true);
  readonly deletingCat = signal<Category | null>(null);
  readonly pageNo = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly first = signal(true);
  readonly last = signal(true);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.catService.getAll(this.pageNo()).subscribe({
      next: res => { this.categories.set(res.content); this.pageNo.set(res.pageNo); this.totalPages.set(res.totalPages); this.totalElements.set(res.totalElements); this.first.set(res.first); this.last.set(res.last); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  onPageChange(p: number): void { this.pageNo.set(p); this.load(); }
  confirmDelete(cat: Category): void { this.deletingCat.set(cat); }
  doDelete(cat: Category): void {
    this.catService.delete(cat.id).subscribe({ next: () => { this.msg.success('Catégorie supprimée'); this.deletingCat.set(null); this.load(); } });
  }
}
