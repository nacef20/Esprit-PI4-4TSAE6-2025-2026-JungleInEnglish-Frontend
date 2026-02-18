import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from '../../../core/services/message.service';

@Component({
    selector: 'app-checkout',
    standalone: true,
    imports: [ReactiveFormsModule],
    template: `
    <div class="container">
      <div class="page-header">
        <h1>📋 Passer la commande</h1>
        <p>Remplissez les informations de livraison</p>
      </div>
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-card">
        <div class="form-row">
          <div class="form-group">
            <label for="firstName">Prénom</label>
            <input id="firstName" class="form-control" formControlName="firstName" />
          </div>
          <div class="form-group">
            <label for="lastName">Nom</label>
            <input id="lastName" class="form-control" formControlName="lastName" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" class="form-control" formControlName="email" />
          </div>
          <div class="form-group">
            <label for="mobileNo">Téléphone</label>
            <input id="mobileNo" class="form-control" formControlName="mobileNo" />
          </div>
        </div>
        <div class="form-group">
          <label for="address">Adresse</label>
          <input id="address" class="form-control" formControlName="address" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="city">Ville</label>
            <input id="city" class="form-control" formControlName="city" />
          </div>
          <div class="form-group">
            <label for="state">Gouvernorat</label>
            <input id="state" class="form-control" formControlName="state" />
          </div>
          <div class="form-group">
            <label for="pincode">Code postal</label>
            <input id="pincode" class="form-control" formControlName="pincode" />
          </div>
        </div>
        <div class="form-group">
          <label for="paymentType">Paiement</label>
          <select id="paymentType" class="form-control" formControlName="paymentType">
            <option value="COD">Paiement à la livraison</option>
            <option value="ONLINE">Paiement en ligne</option>
          </select>
        </div>
        <button type="submit" class="btn btn-primary btn-lg mt-3" [disabled]="form.invalid">✅ Confirmer la commande</button>
      </form>
    </div>
  `,
    styles: `
    :host { display: block; padding-bottom: 3rem; }
    .form-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 2rem; max-width: 700px; }
    .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; }
  `,
})
export class CheckoutComponent {
    private readonly fb = inject(FormBuilder);
    private readonly router = inject(Router);
    private readonly orderService = inject(OrderService);
    private readonly auth = inject(AuthService);
    private readonly msg = inject(MessageService);

    readonly form = this.fb.nonNullable.group({
        firstName: ['', Validators.required],
        lastName: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        mobileNo: ['', Validators.required],
        address: ['', Validators.required],
        city: ['', Validators.required],
        state: ['', Validators.required],
        pincode: ['', Validators.required],
        paymentType: ['COD', Validators.required],
    });

    onSubmit(): void {
        if (this.form.invalid) return;
        const user = this.auth.currentUser();
        if (!user) return;
        this.orderService.placeOrder({ ...this.form.getRawValue(), userId: user.id }).subscribe({
            next: () => {
                this.msg.success('Commande passée avec succès !');
                this.router.navigate(['/my-orders']);
            },
        });
    }
}
