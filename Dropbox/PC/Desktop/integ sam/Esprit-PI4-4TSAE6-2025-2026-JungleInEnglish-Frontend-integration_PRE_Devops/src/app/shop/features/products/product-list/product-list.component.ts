import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../services/product.service';
import { CartService } from '../../../services/cart.service';
import { CategoryService } from '../../../services/category.service';
import { AuthService } from '@shop/services/auth.service';
import { MessageService } from '../../../services/message.service';
import { PaginationComponent } from '@shop/shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '@shop/shared/components/confirm-dialog/confirm-dialog.component';
import type { Product } from '@shop/shared/models/product.model';
import type { Category } from '@shop/shared/models/category.model';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ChatBotComponent } from '@shop/shared/components/chat-bot/chat-bot.component';

@Component({
    selector: 'app-product-list',
    standalone: true,
    imports: [RouterLink, FormsModule, PaginationComponent, DecimalPipe, ConfirmDialogComponent, ChatBotComponent],
    templateUrl: './product-list.component.html',
    styleUrl: './product-list.component.css',
})
export class ProductListComponent implements OnInit, OnDestroy {
    protected readonly productService = inject(ProductService);
    private readonly cartService = inject(CartService);
    private readonly categoryService = inject(CategoryService);
    readonly auth = inject(AuthService);
    private readonly msg = inject(MessageService);

    readonly products = signal<Product[]>([]);
    readonly categories = signal<Category[]>([]);
    readonly loading = signal(true);
    readonly deletingProduct = signal<Product | null>(null);
    readonly search = signal('');
    readonly selectedCategory = signal('');

    readonly pageNo = signal(0);
    readonly totalPages = signal(0);
    readonly totalElements = signal(0);
    readonly first = signal(true);
    readonly last = signal(true);

    private readonly searchSubject = new Subject<string>();
    private readonly destroy$ = new Subject<void>();

    ngOnInit(): void {
        console.log('Current User:', this.auth.currentUser());
        console.log('Is Admin:', this.auth.isAdmin());
        this.categoryService.getActive().subscribe(c => this.categories.set(c));
        this.loadProducts();

        this.searchSubject.pipe(
            debounceTime(300),
            distinctUntilChanged(),
            takeUntil(this.destroy$)
        ).subscribe(term => {
            this.search.set(term);
            this.pageNo.set(0);
            this.loadProducts();
        });
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    loadProducts(): void {
        this.loading.set(true);
        // Changed page size to 4 to show 4 products per page (while still keeping pagination active for the 5th product)
        this.productService.getActive(this.pageNo(), 4, this.selectedCategory(), this.search())
            .subscribe({
                next: res => {
                    this.products.set(res.content);
                    this.pageNo.set(res.pageNo);
                    this.totalPages.set(res.totalPages);
                    this.totalElements.set(res.totalElements);
                    this.first.set(res.first);
                    this.last.set(res.last);
                    this.loading.set(false);
                },
                error: () => this.loading.set(false),
            });
    }

    getCategoryIcon(categoryName: string | undefined): string {
        if (!categoryName) return '';
        const cat = this.categories().find(c => c.name === categoryName);
        return cat?.imageName || '';
    }

    onSearch(term: string): void {
        this.searchSubject.next(term);
    }

    onCategoryFilter(cat: string): void {
        this.selectedCategory.set(cat);
        this.pageNo.set(0);
        this.loadProducts();
    }

    onPageChange(page: number): void {
        this.pageNo.set(page);
        this.loadProducts();
    }

    addToCart(product: Product): void {
        if (!this.auth.isLoggedIn()) { this.msg.error('Please log in to continue'); return; }
        this.cartService.add(product.id).subscribe({
            next: () => this.msg.success(`"${product.title}" added to cart`),
        });
    }

    deleteProduct(p: Product): void {
        this.deletingProduct.set(p);
    }

    doDelete(p: Product): void {
        this.productService.delete(p.id).subscribe({
            next: () => {
                this.msg.success('Product deleted');
                this.deletingProduct.set(null);
                this.loadProducts();
            },
            error: () => {
                this.msg.error('Error deleting product');
                this.deletingProduct.set(null);
            }
        });
    }
}
