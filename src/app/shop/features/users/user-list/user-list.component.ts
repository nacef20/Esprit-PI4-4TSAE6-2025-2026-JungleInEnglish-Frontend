import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ConfirmDialogComponent } from '@shop/shared/components/confirm-dialog/confirm-dialog.component';
import { UserService } from '../../../services/user.service';
import { MessageService } from '../../../services/message.service';
import type { User } from '@shop/shared/models/user.model';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink, ConfirmDialogComponent],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css',
})
export class UserListComponent {
  private readonly userService = inject(UserService);
  private readonly messageService = inject(MessageService);

  readonly users = signal<User[]>([]);
  readonly loading = signal(true);
  readonly deletingUser = signal<User | null>(null);

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
    this.deletingUser.set(user);
  }

  doDelete(user: User): void {
    this.userService.delete(user.id).subscribe({
      next: () => {
        this.users.update((list) => list.filter((u) => u.id !== user.id));
        this.messageService.success('User deleted.');
        this.deletingUser.set(null);
      },
      error: () => this.deletingUser.set(null),
    });
  }
}
