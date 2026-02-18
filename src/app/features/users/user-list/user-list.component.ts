import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { MessageService } from '../../../core/services/message.service';
import type { User } from '../../../shared/models/user.model';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css',
})
export class UserListComponent {
  private readonly userService = inject(UserService);
  private readonly messageService = inject(MessageService);

  readonly users = signal<User[]>([]);
  readonly loading = signal(true);
  readonly deletingId = signal<number | null>(null);

  constructor() {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.userService.getAll().subscribe({
      next: (list) => {
        this.users.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  delete(user: User): void {
    if (!confirm(`Supprimer « ${user.name} » ?`)) return;
    this.deletingId.set(user.id);
    this.userService.delete(user.id).subscribe({
      next: () => {
        this.users.update((list) => list.filter((u) => u.id !== user.id));
        this.messageService.success('Utilisateur supprimé.');
        this.deletingId.set(null);
      },
      error: () => this.deletingId.set(null),
    });
  }
}
