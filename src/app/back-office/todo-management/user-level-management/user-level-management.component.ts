import { Component, OnInit } from '@angular/core';
import { TodoService } from '../../../services/todo.service';
import { Level } from '../../../models/todo.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-user-level-management',
  templateUrl: './user-level-management.component.html',
  styleUrls: ['./user-level-management.component.scss']
})
export class UserLevelManagementComponent implements OnInit {
  students: any[] = [];
  loading = true;
  syncing = false;
  levels = Object.values(Level);

  constructor(private todoService: TodoService) { }

  ngOnInit(): void {
    this.loadStudents();
  }

  loadStudents(): void {
    this.loading = true;
    this.todoService.getAllStudents().subscribe({
      next: (data: any[]) => {
        this.students = data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Failed to load students', err);
        this.loading = false;
      }
    });
  }

  syncData(): void {
    this.syncing = true;
    this.todoService.syncStudents().subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Sync Complete',
          text: 'Student list has been refreshed from Keycloak.',
          timer: 2000,
          showConfirmButton: false
        });
        this.loadStudents();
        this.syncing = false;
      },
      error: (err: any) => {
        console.error('Sync failed', err);
        Swal.fire('Error!', 'Could not sync from Keycloak. Check backend logs.', 'error');
        this.syncing = false;
      }
    });
  }

  onLevelChange(student: any, newLevel: any): void {
    const levelStr = newLevel.target.value;
    this.todoService.updateUserLevel(student.id, levelStr as Level).subscribe({
      next: (updated: any) => {
        student.level = levelStr;
        Swal.fire({
          icon: 'success',
          title: 'Level Updated',
          text: `${student.username}'s level is now ${levelStr}`,
          timer: 1500,
          showConfirmButton: false
        });
      },
      error: (err: any) => {
        Swal.fire('Error!', 'Could not update user level.', 'error');
        this.loadStudents(); // Reload to sync with db
      }
    });
  }
}
