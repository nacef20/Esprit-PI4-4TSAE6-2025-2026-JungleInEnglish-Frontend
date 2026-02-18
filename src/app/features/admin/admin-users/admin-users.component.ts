import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { MessageService } from '../../../core/services/message.service';
import type { User } from '../../../shared/models/user.model';

@Component({
    selector: 'app-admin-users',
    standalone: true,
    imports: [FormsModule],
    template: `
    <div class="container">
      <div class="page-header flex-between">
        <h1>👥 Gestion des utilisateurs</h1>
        <div class="flex gap-1">
          <button class="btn btn-sm" [class.btn-primary]="type() === 1" [class.btn-secondary]="type() !== 1" (click)="switchType(1)">Utilisateurs</button>
          <button class="btn btn-sm" [class.btn-primary]="type() === 2" [class.btn-secondary]="type() !== 2" (click)="switchType(2)">Admins</button>
        </div>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        <div class="table-wrap">
          <table>
            <thead><tr><th>#</th><th>Nom</th><th>Email</th><th>Rôle</th><th>Statut</th><th>Action</th></tr></thead>
            <tbody>
              @for (u of users(); track u.id) {
                <tr>
                  <td>{{ u.id }}</td>
                  <td><strong>{{ u.name }}</strong></td>
                  <td>{{ u.email }}</td>
                  <td><span class="badge badge-primary">{{ u.role }}</span></td>
                  <td>
                    <span class="badge" [class.badge-success]="u.isEnable" [class.badge-danger]="!u.isEnable">
                      {{ u.isEnable ? 'Actif' : 'Désactivé' }}
                    </span>
                  </td>
                  <td>
                    @if (u.isEnable) {
                      <button class="btn btn-sm btn-danger" (click)="toggleStatus(u.id, false)">Désactiver</button>
                    } @else {
                      <button class="btn btn-sm btn-success" (click)="toggleStatus(u.id, true)">Activer</button>
                    }
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="6" class="text-center text-muted" style="padding:2rem;">Aucun utilisateur</td></tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
    styles: `:host { display: block; padding-bottom: 3rem; }`,
})
export class AdminUsersComponent implements OnInit {
    private readonly adminService = inject(AdminService);
    private readonly msg = inject(MessageService);

    readonly users = signal<User[]>([]);
    readonly loading = signal(true);
    readonly type = signal<1 | 2>(1);

    ngOnInit(): void { this.load(); }

    switchType(t: 1 | 2): void { this.type.set(t); this.load(); }

    load(): void {
        this.loading.set(true);
        this.adminService.getUsers(this.type()).subscribe({
            next: u => { this.users.set(u); this.loading.set(false); },
            error: () => this.loading.set(false),
        });
    }

    toggleStatus(id: number, status: boolean): void {
        this.adminService.updateAccountStatus(id, status).subscribe({
            next: () => { this.msg.success('Statut mis à jour'); this.load(); },
        });
    }
}
