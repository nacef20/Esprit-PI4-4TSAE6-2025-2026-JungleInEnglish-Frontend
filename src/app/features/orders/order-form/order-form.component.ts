import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { OrderService } from '../../../core/services/order.service';
import { MessageService } from '../../../core/services/message.service';
import { ValidationService } from '../../../core/services/validation.service';
import type { ProductOrder, OrderStatusEnum } from '../../../shared/models/order.model';

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>✏️ Modifier la commande</h1>
        <p class="text-muted">Commande #{{ orderId() }}</p>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-grid">
          <!-- Product Info (Read Only) -->
          <div class="form-card full-width flex-between">
            <div>
              <h3>Produit</h3>
              <p><strong>{{ productTitle() }}</strong> - {{ productPrice() }} TND</p>
            </div>
            <div style="min-width: 200px;">
              <label for="status">Statut de la commande</label>
              <select id="status" class="form-control" formControlName="status">
                @for (st of statuses(); track st.id) {
                  <option [value]="st.name">{{ st.name }}</option>
                }
              </select>
            </div>
          </div>

          <!-- Quantity -->
          <div class="form-card">
            <h3>Quantité</h3>
            <div class="form-group">
              <label for="quantity">Nombre d'articles</label>
              <input id="quantity" type="number" class="form-control" formControlName="quantity" min="1" />
            </div>
          </div>

          <!-- Shipping Address -->
          <div class="form-card full-width" formGroupName="orderAddress">
            <h3>Adresse de livraison</h3>
            <div class="grid-2">
              <div class="form-group">
                <label for="firstName">Prénom</label>
                <input id="firstName" class="form-control" formControlName="firstName" />
              </div>
              <div class="form-group">
                <label for="lastName">Nom</label>
                <input id="lastName" class="form-control" formControlName="lastName" />
              </div>
              <div class="form-group">
                <label for="email">Email</label>
                <input id="email" type="email" class="form-control" formControlName="email" />
              </div>
              <div class="form-group">
                <label for="telephone">Téléphone</label>
                <input id="telephone" class="form-control" formControlName="telephone" [class.is-invalid]="vs.getErrorMessage(form.controls.orderAddress, 'telephone')" />
                @if (vs.getErrorMessage(form.controls.orderAddress, 'telephone'); as msg) { <span class="form-error">{{ msg }}</span> }
              </div>
              <div class="form-group full-width">
                <label for="address">Adresse</label>
                <textarea id="address" class="form-control" formControlName="address" rows="2"></textarea>
              </div>
              <div class="form-group">
                <label for="city">Ville</label>
                <input id="city" class="form-control" formControlName="city" />
              </div>
              <div class="form-group">
                <label for="state">État / Région</label>
                <input id="state" class="form-control" formControlName="state" />
              </div>
              <div class="form-group">
                <label for="postalCode">Code Postal</label>
                <input id="postalCode" class="form-control" formControlName="postalCode" [class.is-invalid]="vs.getErrorMessage(form.controls.orderAddress, 'postalCode')" />
                @if (vs.getErrorMessage(form.controls.orderAddress, 'postalCode'); as msg) { <span class="form-error">{{ msg }}</span> }
              </div>
            </div>
          </div>

          <div class="flex gap-2 mt-3 full-width">
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid">Mettre à jour</button>
            <button type="button" class="btn btn-secondary" (click)="goBack()">Annuler</button>
          </div>
        </form>
      }
    </div>
  `,
  styles: `
    :host { display: block; padding-bottom: 3rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; max-width: 800px; }
    .full-width { grid-column: 1 / -1; }
    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 1.5rem; }
    .form-card h3 { margin-bottom: 1rem; font-size: 1.1rem; color: var(--color-primary); border-bottom: 1px solid var(--color-border); padding-bottom: .5rem; }
    textarea.form-control { resize: vertical; }
  `,
})
export class OrderFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly orderService = inject(OrderService);
  private readonly msg = inject(MessageService);
  protected readonly vs = inject(ValidationService);

  readonly loading = signal(true);
  readonly orderId = signal('');
  readonly productTitle = signal('');
  readonly productPrice = signal(0);
  readonly statuses = signal<OrderStatusEnum[]>([]);
  private internalId = 0;

  readonly form = this.fb.nonNullable.group({
    quantity: [1, [Validators.required, Validators.min(1)]],
    status: ['', Validators.required],
    orderAddress: this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      address: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      postalCode: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
    }),
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.internalId = +id;
      this.loadData();
    } else {
      this.router.navigate(['/my-orders']);
    }
  }

  loadData(): void {
    this.loading.set(true);
    forkJoin({
      order: this.orderService.getById(this.internalId),
      stList: this.orderService.getStatuses()
    }).subscribe({
      next: ({ order, stList }) => {
        this.statuses.set(stList);
        this.orderId.set(order.orderId);
        this.productTitle.set(order.product?.title ?? 'Produit');
        this.productPrice.set(order.price);

        this.form.patchValue({
          quantity: order.quantity,
          status: order.status,
          orderAddress: {
            firstName: order.orderAddress?.firstName,
            lastName: order.orderAddress?.lastName,
            email: order.orderAddress?.email,
            telephone: order.orderAddress?.telephone,
            address: order.orderAddress?.address,
            city: order.orderAddress?.city,
            state: order.orderAddress?.state,
            postalCode: order.orderAddress?.postalCode,
          },
        });
        this.loading.set(false);
      },
      error: () => {
        this.msg.error('Erreur lors du chargement des données');
        this.router.navigate(['/my-orders']);
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const data = this.form.getRawValue();
    this.orderService.update(this.internalId, data).subscribe({
      next: () => {
        this.msg.success('Commande mise à jour');
        this.router.navigate(['/my-orders']);
      },
      error: (err) => {
        this.vs.handleBackendErrors(this.form.controls.orderAddress, err);
        if (!err.error?.fieldErrors) {
          this.msg.error('Erreur lors de la mise à jour');
        }
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/my-orders']);
  }
}
