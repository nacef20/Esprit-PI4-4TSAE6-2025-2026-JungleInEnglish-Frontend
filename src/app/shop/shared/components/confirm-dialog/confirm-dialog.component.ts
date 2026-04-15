import { Component, input, output } from '@angular/core';

@Component({
    selector: 'app-confirm-dialog',
    standalone: true,
    template: `
    <div class="dialog-backdrop" (click)="cancel.emit()">
      <div class="dialog" (click)="$event.stopPropagation()">
        <h3 class="dialog__title">{{ title() }}</h3>
        <p class="dialog__text">{{ message() }}</p>
        <div class="dialog__actions">
          <button class="btn btn-secondary" (click)="cancel.emit()">Cancel</button>
          <button class="btn btn-danger" (click)="confirm.emit()">Confirm</button>
        </div>
      </div>
    </div>
  `,
  styles: `
    .dialog-backdrop {
      position: fixed; inset: 0;
      background: rgba(15, 23, 42, 0.5);
      backdrop-filter: blur(4px);
      display: flex; align-items: center; justify-content: center;
      z-index: 9999;
      animation: fadeIn 0.15s ease-out;
    }
    .dialog {
      background: white; border-radius: var(--radius-lg);
      padding: 1.75rem; width: 100%; max-width: 420px;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
      animation: dialogPop 0.25s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .dialog__title { margin: 0 0 0.75rem 0; font-size: 1.25rem; font-weight: 800; color: #0f172a; }
    .dialog__text { margin: 0 0 1.5rem 0; color: #475569; line-height: 1.5; font-size: 0.95rem; }
    .dialog__actions { display: flex; justify-content: flex-end; gap: 0.75rem; }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes dialogPop {
      0% { opacity: 0; transform: scale(0.95) translateY(10px); }
      100% { opacity: 1; transform: scale(1) translateY(0); }
    }
  `
})
export class ConfirmDialogComponent {
    readonly title = input('Confirm');
    readonly message = input('Are you sure?');
    readonly confirm = output<void>();
    readonly cancel = output<void>();
}
