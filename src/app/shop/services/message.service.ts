import { Injectable, signal, computed } from '@angular/core';

export type MessageType = 'success' | 'error' | 'info';

export interface AppMessage {
  type: MessageType;
  text: string;
}

/**
 * Service global pour afficher des messages à l'utilisateur (succès, erreur, info).
 * Utilisé par l'intercepteur HTTP et par les composants.
 */
@Injectable({ providedIn: 'root' })
export class MessageService {
  private readonly messages = signal<AppMessage[]>([]);

  readonly current = computed(() => this.messages()[0] ?? null);
  readonly hasMessage = computed(() => this.messages().length > 0);

  success(text: string): void {
    this.add({ type: 'success', text });
  }

  error(text: string): void {
    this.add({ type: 'error', text });
  }

  info(text: string): void {
    this.add({ type: 'info', text });
  }

  private add(message: AppMessage): void {
    this.messages.update((list) => [message, ...list]);
    // Auto-dismiss après 5 secondes
    setTimeout(() => this.dismiss(), 5000);
  }

  dismiss(): void {
    this.messages.update((list) => list.slice(1));
  }

  clear(): void {
    this.messages.set([]);
  }
}
