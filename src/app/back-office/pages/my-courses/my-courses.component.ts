import { Component, OnInit } from '@angular/core';
import { CoursesService } from '../../../services/courses.service';
import { LessonsService } from '../../../services/lessons.service';
import { Course } from '../../../models/course.model';
import { AuthService } from '../../../auth/auth.service';


@Component({
  selector: 'app-my-courses',
  templateUrl: './my-courses.component.html',
  styleUrls: ['./my-courses.component.scss']
})
export class MyCoursesComponent implements OnInit {

  myCourses: Course[] = [];
  loading = false;
  error: string | null = null;
  suggestedCourses: Course[] = [];
  loadingSuggested = false;
  isStudent = false;

  constructor(
    private lessonsService: LessonsService,
    private coursesService: CoursesService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadMyCourses();
    this.loadSuggestedCourses();
      this.isStudent = this.authService.isStudent();
  }

  loadMyCourses() {
    this.loading = true;
    this.error = null;

    this.lessonsService.getMyCourseIds().subscribe({
      next: (ids) => {
        if (!ids || ids.length === 0) {
          this.myCourses = [];
          this.loading = false;
          return;
        }

        this.coursesService.getCoursesByIds(ids).subscribe({
          next: (courses) => {
            this.myCourses = courses;
            this.loading = false;
          },
          error: (err) => {
            console.error(err);
            this.error = 'Failed to load course details.';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        console.error(err);
        this.error = 'Failed to load enrolled courses.';
        this.loading = false;
      }
    });
  }

  loadSuggestedCourses() {
    this.loadingSuggested = true;

    this.lessonsService.getTop3SuggestedCourseIds().subscribe({
      next: (ids) => {
        if (!ids?.length) {
          this.suggestedCourses = [];
          this.loadingSuggested = false;
          return;
        }

        this.coursesService.getCoursesByIds(ids).subscribe({
          next: (courses) => {
            // keep the same order as ids
            this.suggestedCourses = ids
              .map(id => courses.find(c => c.id === id))
              .filter(Boolean) as Course[];

            this.loadingSuggested = false;
          },
          error: (e) => {
            console.error('Load suggested details failed', e);
            this.suggestedCourses = [];
            this.loadingSuggested = false;
          }
        });
      },
      error: (e) => {
        console.error('Load suggested ids failed', e);
        this.suggestedCourses = [];
        this.loadingSuggested = false;
      }
    });
  }

  enroll(courseId: number) {
  this.lessonsService.enrollCourse(courseId).subscribe({
    next: () => {
      // refresh both lists
      this.loadMyCourses();
      this.loadSuggestedCourses();
    },
    error: (e) => console.error('Enroll failed', e)
  });
}
}