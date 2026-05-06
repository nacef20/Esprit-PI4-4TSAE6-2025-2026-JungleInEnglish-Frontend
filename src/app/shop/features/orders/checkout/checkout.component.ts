import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { OrderService } from '../../../services/order.service';
import { AuthService } from '@shop/services/auth.service';
import { MessageService } from '../../../services/message.service';
import { ValidationService } from '../../../services/validation.service';
import { CartService } from '../../../services/cart.service';
import { PaymentService } from '../../../services/payment.service';
import type { Cart } from '@shop/shared/models/cart.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>📋 Place an Order</h1>
        <p>Fill in your delivery information</p>
      </div>
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-card">
        <div class="form-row">
          <div class="form-group">
            <label for="firstName">First Name</label>
            <input id="firstName" class="form-control" formControlName="firstName" />
          </div>
          <div class="form-group">
            <label for="lastName">Last Name</label>
            <input id="lastName" class="form-control" formControlName="lastName" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" class="form-control" formControlName="email" />
          </div>
          <div class="form-group">
            <label for="telephone">Phone</label>
            <input id="telephone" class="form-control" formControlName="telephone" [class.is-invalid]="vs.getErrorMessage(form, 'telephone')" />
            @if (vs.getErrorMessage(form, 'telephone'); as msg) { <span class="form-error">{{ msg }}</span> }
          </div>
        </div>
        <div class="form-group">
          <label for="address">Address</label>
          <input id="address" class="form-control" formControlName="address" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="city">City</label>
            <input id="city" class="form-control" formControlName="city" />
          </div>
          <div class="form-group">
            <label for="state">Governorate</label>
            <input id="state" class="form-control" formControlName="state" />
          </div>
          <div class="form-group">
            <label for="postalCode">Postal Code</label>
            <input id="postalCode" class="form-control" formControlName="postalCode" [class.is-invalid]="vs.getErrorMessage(form, 'postalCode')" />
            @if (vs.getErrorMessage(form, 'postalCode'); as msg) { <span class="form-error">{{ msg }}</span> }
          </div>
        </div>
        <div class="form-group">
          <label for="paymentType">Payment Method</label>
          <select id="paymentType" class="form-control" formControlName="paymentType">
            <option value="COD">Cash on Delivery</option>
            <option value="ONLINE">Online Payment</option>
          </select>
        </div>
        <button type="submit" class="btn btn-primary btn-lg mt-3" [disabled]="form.invalid || hasInsufficientStock() || isProcessing()">
          @if (isProcessing()) {
            <span>⏳ Processing...</span>
          } @else if (hasInsufficientStock()) {
            <span>❌ Insufficient Stock</span>
          } @else {
            <span>✅ Confirm Order</span>
          }
        </button>
      </form>
    </div>
  `,
  styles: `
    :host { display: block; padding-bottom: 3rem; }
    .form-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 2rem; max-width: 700px; }
    .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; }
  `,
})
export class CheckoutComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly orderService = inject(OrderService);
  private readonly auth = inject(AuthService);
  private readonly cartService = inject(CartService);
  private readonly paymentService = inject(PaymentService);
  private readonly msg = inject(MessageService);
  protected readonly vs = inject(ValidationService);

  readonly cartItems = signal<Cart[]>([]);
  readonly hasInsufficientStock = computed(() => this.cartItems().some(i => i.quantity > i.product.stock));
  readonly isProcessing = signal(false);

  ngOnInit(): void {
    this.cartService.getMyCart().subscribe(items => this.cartItems.set(items));
  }

  readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telephone: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    address: ['', Validators.required],
    city: ['', Validators.required],
    state: ['', Validators.required],
    postalCode: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
    paymentType: ['COD', Validators.required],
  });

  onSubmit(): void {
    if (this.form.invalid || this.isProcessing()) return;

    this.isProcessing.set(true);
    const formValue = this.form.getRawValue();
    const paymentType = formValue.paymentType;

    if (paymentType === 'ONLINE') {
      this.msg.info('Initiating payment session...');
      // compute total using discountPrice if applicable
      const totalAmount = this.cartItems().reduce((acc, curr) => {
        const itemPrice = (curr.product.discount && curr.product.discount > 0 && curr.product.discountPrice) ? curr.product.discountPrice : curr.product.price;
        return acc + (curr.quantity * itemPrice);
      }, 0);
      
      let detailsText = "Product\n";
      this.cartItems().forEach(item => {
        const itemPrice = (item.product.discount && item.product.discount > 0 && item.product.discountPrice) ? item.product.discountPrice : item.product.price;
        detailsText += `${item.product.title}\n${itemPrice.toFixed(2)} TND x ${item.quantity}\n`;
      });
      detailsText += `\nTotal\n${totalAmount.toFixed(2)} TND\n\n`;
      detailsText += `Shipping Address\n${formValue.firstName} ${formValue.lastName}\n\n${formValue.address}\n\n${formValue.city}, ${formValue.state} ${formValue.postalCode}\n\n📞 ${formValue.telephone}\n\n✉️ ${formValue.email}`;
      
      this.paymentService.initiate({ 
        email: formValue.email, 
        amount: totalAmount,
        orderDetailsText: detailsText
      }).subscribe({
        next: (res) => {
          this.isProcessing.set(false);
          // Navigate to payment page with router state
          this.router.navigate(['/shop/payment'], {
            state: {
              sessionId: res.sessionId,
              orderData: formValue
            }
          });
        },
        error: (err) => {
          this.isProcessing.set(false);
          this.msg.error('Failed to initiate payment: ' + (err.error?.message || err.message));
        }
      });
    } else {
      this.placeOrder();
    }
  }

  private placeOrder(): void {
    this.orderService.placeOrder(this.form.getRawValue()).subscribe({
      next: () => {
        this.msg.success('Order placed successfully!');
        this.isProcessing.set(false);
        this.router.navigate(['/shop/my-orders']);
      },
      error: (err) => {
        this.vs.handleBackendErrors(this.form, err);
        if (!err.error?.fieldErrors) {
          this.msg.error('Failed to place order');
        }
        this.isProcessing.set(false);
      }
    });
  }
}
