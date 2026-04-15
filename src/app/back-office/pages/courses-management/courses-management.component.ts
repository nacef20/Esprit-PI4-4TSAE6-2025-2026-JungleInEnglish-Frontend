import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CoursesService } from '../../../services/courses.service';
import { LessonsService } from '../../../services/lessons.service';
import { Course, CourseLevel } from '../../../models/course.model';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-courses-management',
  standalone: false,
  templateUrl: './courses-management.component.html',
  styleUrls: ['./courses-management.component.scss']
})
export class CoursesManagementComponent implements OnInit {
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  loading = true;
  error: string | null = null;

  // Modal
  showModal = false;
  isEditMode = false;
  saving = false;

  // Filters
  searchTerm = '';
  filterLevel = '';

  // Current course being edited/created
  currentCourse: Course = this.getEmptyCourse();

constructor(
  private coursesService: CoursesService,
  private lessonsService: LessonsService,
   private authService: AuthService
) {}
  ngOnInit(): void {
    this.loadCourses();
  }

  isTeacher(): boolean {
  return this.authService.getRoles().includes('TEACHER');
}

isStudent(): boolean {
  return this.authService.getRoles().includes('STUDENT');
}

loadCourses(): void {
  this.loading = true;
  this.error = null;

  this.coursesService.getAllCourses().subscribe({
    next: (data) => {
      this.courses = data;
      this.filteredCourses = data;   // ✅
      this.loading = false;
    },
    error: (err) => {
      this.error = 'Error loading courses';
      this.loading = false;
      console.error(err);
    }
  });
}

filterCourses(): void {
  this.loading = true;
  this.error = null;

  this.coursesService.getAllCourses(this.searchTerm, this.filterLevel).subscribe({
    next: (data) => {
      this.courses = data;
      this.filteredCourses = data;   // ✅ THIS makes the table update
      this.loading = false;
    },
    error: (err) => {
      this.error = 'Error loading courses';
      this.loading = false;
      console.error(err);
    }
  });
}

  getCountByLevel(level: string): number {
    return this.courses.filter(c => c.level === level).length;
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.currentCourse = this.getEmptyCourse();
    this.showModal = true;
  }

  editCourse(course: Course): void {
    this.isEditMode = true;
    this.currentCourse = { ...course };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.currentCourse = this.getEmptyCourse();
  }

  saveCourse(): void {
    if (!this.currentCourse.name || !this.currentCourse.description ||
      !this.currentCourse.duration || !this.currentCourse.level) {
      alert('Please fill in all required fields');
      return;
    }

    this.saving = true;

    if (!this.isEditMode) {
      this.currentCourse.professorId = this.authService.getUserInfo()?.id || '';
    }

    const operation = this.isEditMode
      ? this.coursesService.updateCourse(this.currentCourse.id!, this.currentCourse)
      : this.coursesService.createCourse(this.currentCourse);

    operation.subscribe({
      next: () => {
        this.saving = false;
        this.closeModal();
        this.loadCourses();
        alert(this.isEditMode ? 'Course updated!' : 'Course created!');
      },
      error: (err) => {
        console.error(err);
        this.saving = false;

        const msg = this.extractErrorMessage(err, 'Error saving course');
        alert(msg);
      }
    });
  }

  deleteCourse(course: Course): void {
    if (!confirm(`are you sure you want to delete "${course.name}"?`)) {
      return;
    }

    this.coursesService.deleteCourse(course.id!).subscribe({
      next: () => {
        this.loadCourses();
        alert('Cours deleted!');
      },
      error: (err) => {
        console.error(err);
        alert(this.extractErrorMessage(err, 'Error deleting course'));
      }
    });
  }

  getLevelClass(level: CourseLevel): string {
    switch (level) {
      case CourseLevel.BEGINNER:
        return 'level-beginner';
      case CourseLevel.INTERMEDIATE:
        return 'level-intermediate';
      case CourseLevel.ADVANCED:
        return 'level-advanced';
      default:
        return '';
    }
  }

  getLevelLabel(level: CourseLevel): string {
    switch (level) {
      case CourseLevel.BEGINNER:
        return 'Débutant';
      case CourseLevel.INTERMEDIATE:
        return 'Intermédiaire';
      case CourseLevel.ADVANCED:
        return 'Avancé';
      default:
        return level;
    }
  }

  getDefaultImage(): string {
    return 'https://images.unsplash.com/photo-1516259762381-22954d7d3ad2?w=800';
  }

  private getEmptyCourse(): Course {
    return {
      name: '',
      description: '',
      duration: '',
      level: CourseLevel.BEGINNER,
      imageUrl: '',
      professorId: ''
    };
  }

  private extractErrorMessage(err: any, fallback: string): string {
    // backend might return {message: "..."} or plain text or nested error
    return (
      err?.error?.message ||
      err?.error?.error ||
      (typeof err?.error === 'string' ? err.error : null) ||
      err?.message ||
      fallback
    );
  }
  enroll(courseId: number) {
  this.lessonsService.enrollCourse(courseId).subscribe({
    next: () => {
      alert('Course added to My Courses ✅');
    },
    error: (err) => {
      console.error('Enroll failed', err);
      alert('Enroll failed ❌');
    }
  });
}
}
