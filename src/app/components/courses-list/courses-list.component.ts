import { Component, OnInit } from '@angular/core';
import { CoursesService } from '../../services/courses.service';
import { Course, CourseLevel } from '../../models/course.model';


@Component({
  selector: 'app-courses-list',
  templateUrl: './courses-list.component.html',
  styleUrls: ['./courses-list.component.scss']
})
export class CoursesListComponent implements OnInit {
  courses: Course[] = [];
  loading = true;
  error: string | null = null;

  selectedCourse: Course | null = null;
  showCourseOverlay = false;

  constructor(private coursesService: CoursesService) { }

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading = true;
    this.error = null;

    this.coursesService.getAllCourses().subscribe({
      next: (data) => {
        console.log('Courses loaded:', data);
        this.courses = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading courses:', err);

        // Try to extract a useful message from backend
        const msg =
          err?.error?.message ||
          err?.error?.error ||
          (typeof err?.error === 'string' ? err.error : null) ||
          err?.message ||
          `Request failed (${err?.status || 'unknown'})`;

        this.error = msg;
        this.loading = false;
      }
    });
  }
  openCourseDetails(course: Course) {
    this.selectedCourse = course;
    this.showCourseOverlay = true;
  }

  closeCourseDetails(): void {
    this.showCourseOverlay = false;
    this.selectedCourse = null;
  }

  getLevelBadgeClass(level: CourseLevel): string {
    switch (level) {
      case CourseLevel.BEGINNER:
        return 'badge-beginner';
      case CourseLevel.INTERMEDIATE:
        return 'badge-intermediate';
      case CourseLevel.ADVANCED:
        return 'badge-advanced';
      default:
        return 'badge-default';
    }
  }
  debugClick(course: any) {
    console.log('clicked', course);
  }
  getDefaultImage(): string {
    return 'https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=800';
  }
}