import { Component, input, output } from '@angular/core';

@Component({
    selector: 'app-pagination',
    standalone: true,
    template: `
    <div class="pagination">
      <button (click)="pageChange.emit(0)" [disabled]="first()">⟪</button>
      <button (click)="pageChange.emit(pageNo() - 1)" [disabled]="first()">← Prev</button>
      @for (p of pages(); track p) {
        <button [class.active]="p === pageNo()" (click)="pageChange.emit(p)">{{ p + 1 }}</button>
      }
      <button (click)="pageChange.emit(pageNo() + 1)" [disabled]="last()">Next →</button>
      <button (click)="pageChange.emit(totalPages() - 1)" [disabled]="last()">⟫</button>
      <span class="pagination__info">{{ totalElements() }} résultats</span>
    </div>
  `,
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
