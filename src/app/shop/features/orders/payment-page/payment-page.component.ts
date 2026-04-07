import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaymentService } from '../../../services/payment.service';
import { OrderService } from '../../../services/order.service';
import { MessageService } from '../../../services/message.service';
import { ValidationService } from '../../../services/validation.service';

@Component({
  selector: 'app-payment-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>💳 Secure Payment</h1>
        <p>Complete your payment to finalize the order</p>
      </div>

      <div class="form-card mx-auto" style="max-width: 500px;">
        <form [formGroup]="paymentForm" (ngSubmit)="onConfirm()">
          <div class="form-group">
            <label for="cardNumber">Card Number</label>
            <input id="cardNumber" type="text" class="form-control" formControlName="cardNumber" placeholder="0000 0000 0000 0000" maxlength="16" />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="expiryDate">Expiry Date</label>
              <input id="expiryDate" type="text" class="form-control" formControlName="expiryDate" placeholder="MM/YY" maxlength="5" />
            </div>
            <div class="form-group">
              <label for="cvv">CVV</label>
              <input id="cvv" type="text" class="form-control" formControlName="cvv" placeholder="123" maxlength="3" />
            </div>
          </div>

          <button type="submit" class="btn btn-primary w-100 mt-4" [disabled]="paymentForm.invalid || isProcessing()">
            @if (isProcessing()) {
              <span>⏳ Processing Payment...</span>
            } @else {
              <span>Pay Now</span>
            }
          </button>
        </form>
      </div>
    </div>
  `,
  styles: `
    :host { display: block; padding-bottom: 3rem; }
    .form-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 2rem; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .mx-auto { margin-left: auto; margin-right: auto; }
    .w-100 { width: 100%; }
    .mt-4 { margin-top: 1.5rem; }
  `
})
export class PaymentPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly paymentService = inject(PaymentService);
  private readonly orderService = inject(OrderService);
  private readonly msg = inject(MessageService);
  protected readonly vs = inject(ValidationService);

  readonly isProcessing = signal(false);
  private sessionId = '';
  private pendingOrderData: any = null;

  ngOnInit() {
    // Retrieve navigation state
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state) {
      this.sessionId = navigation.extras.state['sessionId'];
      this.pendingOrderData = navigation.extras.state['orderData'];
    } else {
      // Fallback for page reload
      const historyState = history.state;
      if (historyState && historyState.sessionId) {
        this.sessionId = historyState.sessionId;
        this.pendingOrderData = historyState.orderData;
      }
    }

    if (!this.sessionId || !this.pendingOrderData) {
      this.msg.error('Payment session invalid. Restarting checkout.');
      this.router.navigate(['/shop/checkout']);
    }
  }

  readonly paymentForm = this.fb.nonNullable.group({
    cardNumber: ['', [Validators.required, Validators.minLength(16), Validators.maxLength(16)]],
    expiryDate: ['', [Validators.required]],
    cvv: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]]
  });

  onConfirm(): void {
    if (this.paymentForm.invalid || this.isProcessing() || !this.sessionId) return;
    this.isProcessing.set(true);

    const payload = {
      sessionId: this.sessionId,
      cardNumber: this.paymentForm.value.cardNumber!,
      expiryDate: this.paymentForm.value.expiryDate!,
      cvv: this.paymentForm.value.cvv!
    };

    this.paymentService.confirm(payload).subscribe({
      next: (res) => {
        if (res.status === 'SUCCESS') {
          this.msg.success(res.message);
          this.placeActualOrder();
        } else {
          this.msg.error('Payment failed: ' + res.message);
          this.isProcessing.set(false);
        }
      },
      error: (err) => {
        this.msg.error(err.error?.message || 'Payment confirmation failed');
        this.isProcessing.set(false);
      }
    });
  }

  private placeActualOrder() {
    this.orderService.placeOrder(this.pendingOrderData).subscribe({
      next: () => {
        this.msg.success('Order placed successfully!');
        this.isProcessing.set(false);
        this.router.navigate(['/shop/my-orders']);
      },
      error: (err) => {
        this.msg.error('Order creation failed after payment.');
        this.isProcessing.set(false);
      }
    });
  }
}
