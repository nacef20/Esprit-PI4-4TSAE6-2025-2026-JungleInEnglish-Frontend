import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { CategoryService } from '../../../core/services/category.service';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from '../../../core/services/message.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import type { Product } from '../../../shared/models/product.model';
import type { Category } from '../../../shared/models/category.model';

@Component({
    selector: 'app-product-list',
    standalone: true,
    imports: [RouterLink, FormsModule, PaginationComponent, DecimalPipe],
    templateUrl: './product-list.component.html',
    styleUrl: './product-list.component.css',
})
export class ProductListComponent implements OnInit {
    protected readonly productService = inject(ProductService);
    private readonly cartService = inject(CartService);
    private readonly categoryService = inject(CategoryService);
    readonly auth = inject(AuthService);
    private readonly msg = inject(MessageService);

    readonly products = signal<Product[]>([]);
    readonly categories = signal<Category[]>([]);
    readonly loading = signal(true);
    readonly search = signal('');
    readonly selectedCategory = signal('');

    readonly pageNo = signal(0);
    readonly totalPages = signal(0);
    readonly totalElements = signal(0);
    readonly first = signal(true);
    readonly last = signal(true);

    ngOnInit(): void {
        console.log('Current User:', this.auth.currentUser());
        console.log('Is Admin:', this.auth.isAdmin());
        this.categoryService.getActive().subscribe(c => this.categories.set(c));
        this.loadProducts();
    }

    loadProducts(): void {
        this.loading.set(true);
        this.productService.getActive(this.pageNo(), 12, this.selectedCategory(), this.search())
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

    onSearch(term: string): void {
        this.search.set(term);
        this.pageNo.set(0);
        this.loadProducts();
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
        if (!this.auth.isLoggedIn()) { this.msg.error('Veuillez vous connecter'); return; }
        this.cartService.add(product.id).subscribe({
            next: () => this.msg.success(`"${product.title}" ajouté au panier`),
        });
    }

    deleteProduct(p: Product): void {
        if (!confirm(`Supprimer « ${p.title} » ?`)) return;
        this.productService.delete(p.id).subscribe({
            next: () => {
                this.msg.success('Produit supprimé');
                this.loadProducts();
            },
            error: () => this.msg.error('Erreur lors de la suppression'),
        });
    }
}
