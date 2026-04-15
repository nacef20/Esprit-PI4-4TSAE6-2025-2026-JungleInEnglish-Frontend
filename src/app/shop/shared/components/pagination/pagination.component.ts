import { Component, input, output } from '@angular/core';

@Component({
    selector: 'app-pagination',
    standalone: true,
    template: `
    @if (totalPages() > 1) {
    <div class="pagination">
      <button (click)="pageChange.emit(0)" [disabled]="first()">⟪</button>
      <button (click)="pageChange.emit(pageNo() - 1)" [disabled]="first()">← Prev</button>
      @for (p of pages(); track p) {
        <button [class.active]="p === pageNo()" (click)="pageChange.emit(p)">{{ p + 1 }}</button>
      }
      <button (click)="pageChange.emit(pageNo() + 1)" [disabled]="last()">Next →</button>
      <button (click)="pageChange.emit(totalPages() - 1)" [disabled]="last()">⟫</button>
      <span class="pagination__info">{{ totalElements() }} results</span>
    </div>
    } @else if (totalElements() > 0) {
    <div class="pagination">
      <span class="pagination__info" style="margin-left: 0;">Showing all {{ totalElements() }} results</span>
    </div>
    }
  `,
    styles: `
    .pagination {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      flex-wrap: wrap;
      margin-top: 2rem;
    }
    .pagination button {
      padding: 0.5rem 0.75rem;
      min-width: 36px;
      border: 1px solid #e2e8f0;
      background: #ffffff;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 500;
      color: #64748b;
      transition: all 0.2s;
    }
    .pagination button:hover:not(:disabled) {
      background: #f1f5f9;
      color: #10b981;
      border-color: #10b981;
    }
    .pagination button.active {
      background: #10b981;
      color: white;
      border-color: #10b981;
    }
    .pagination button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .pagination__info {
      margin-left: 1rem;
      font-size: 0.875rem;
      color: #94a3b8;
      font-weight: 600;
      background: #f8fafc;
      padding: 0.5rem 1rem;
      border-radius: 20px;
    }
  `
})
export class PaginationComponent {
    readonly pageNo = input(0);
    readonly totalPages = input(0);
    readonly totalElements = input(0);
    readonly first = input(true);
    readonly last = input(true);
    readonly pageChange = output<number>();

    pages(): number[] {
        const total = this.totalPages();
        const current = this.pageNo();
        const start = Math.max(0, current - 2);
        const end = Math.min(total, current + 3);
        const arr: number[] = [];
        for (let i = start; i < end; i++) arr.push(i);
        return arr;
    }
}
