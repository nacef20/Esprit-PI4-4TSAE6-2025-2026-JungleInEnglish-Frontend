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
          <button class="btn btn-secondary" (click)="cancel.emit()">Annuler</button>
          <button class="btn btn-danger" (click)="confirm.emit()">Confirmer</button>
        </div>
      </div>
    </div>
  `,
})
export class ConfirmDialogComponent {
    readonly title = input('Confirmer');
    readonly message = input('Êtes-vous sûr ?');
    readonly confirm = output<void>();
    readonly cancel = output<void>();
}
