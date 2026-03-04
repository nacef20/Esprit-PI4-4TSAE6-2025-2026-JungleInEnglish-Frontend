import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User } from '../service/user.model';
import { AuthService } from '../service/auth.service';
import { Router } from '@angular/router';
import { NavbarComponent } from './navbar/navbar.component';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  userForm: Partial<User> = { name: '', email: '', password: '', role: 'CANDIDATE' };
  editingUserId: number | null = null;
  error: string | null = null;
  success: string | null = null;
  roles: string[] = ['ADMIN', 'EMPLOYER', 'CANDIDATE'];

  constructor(private http: HttpClient, private authService: AuthService, private router: Router) { }

  ngOnInit() {
    this.fetchUsers();
  }

  fetchUsers() {
    this.http.get<User[]>('http://localhost:8880/api/auth/all').subscribe({
      next: users => this.users = users,
      error: () => this.error = 'Failed to fetch users'
    });
  }

  createUser() {
    if (!this.userForm.name || !this.userForm.email || !this.userForm.role || !this.userForm.password) {
      this.error = 'Name, Email, Password, and Role are required';
      return;
    }
    this.http.post<User>('http://localhost:8880/api/auth/add', this.userForm).subscribe({
      next: () => {
        this.success = 'User created successfully';
        this.error = null;
        this.userForm = { name: '', email: '', password: '', role: 'CANDIDATE' };
        this.fetchUsers();
      },
      error: () => this.error = 'Failed to create user'
    });
  }

  editUser(user: User) {
    this.editingUserId = user.id || null;
    this.userForm = { ...user };
  }

  updateUser() {
    if (!this.editingUserId || !this.userForm.role) return;
    const updatePayload = { ...this.userForm };
    if (!updatePayload.password) delete updatePayload.password;
    this.http.put<User>(`http://localhost:8880/api/auth/update/${this.editingUserId}`, updatePayload).subscribe({
      next: () => {
        this.success = 'User updated successfully';
        this.error = null;
        this.editingUserId = null;
        this.userForm = { name: '', email: '', password: '', role: 'CANDIDATE' };
        this.fetchUsers();
      },
      error: () => this.error = 'Failed to update user'
    });
  }

  deleteUser(id: number) {
    if (!confirm('Are you sure you want to delete this user?')) return;
    this.http.delete(`http://localhost:8880/api/auth/delete/${id}`).subscribe({
      next: () => {
        this.success = 'User deleted successfully';
        this.error = null;
        this.fetchUsers();
      },
      error: () => this.error = 'Failed to delete user'
    });
  }

  cancelEdit() {
    this.editingUserId = null;
    this.userForm = { name: '', email: '', password: '', role: 'CANDIDATE' };
  }

  goToJobOffers() {
    this.router.navigate(['/admin']);
  }
} 