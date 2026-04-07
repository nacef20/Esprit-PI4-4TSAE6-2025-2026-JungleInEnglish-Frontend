import { Component, OnInit } from '@angular/core';
import { CoursesService } from '../../../services/courses.service';
import { CourseProgressSummary, LessonsService } from '../../../services/lessons.service';
import { Course } from '../../../models/course.model';
import { LessonAttachment, AttachmentType } from '../../../models/lesson-attachment.model';

@Component({
  selector: 'app-lessons-management',
  templateUrl: './lessons-management.component.html',
  styleUrls: ['./lessons-management.component.scss']
})
export class LessonsManagementComponent implements OnInit {
  courses: Course[] = [];
  selectedCourseId: number | null = null;
  progressSummary!: CourseProgressSummary;

  // ✅ Attachments admin panel state
  selectedLessonId: number | null = null;
  attachments: LessonAttachment[] = [];
  selectedType: AttachmentType = 'PDF';
  selectedFile: File | null = null;

  loading = false;
  error: string | null = null;
  message: string | null = null;

  constructor(
    private coursesService: CoursesService,
    private lessonsService: LessonsService
  ) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading = true;
    this.error = null;

    this.coursesService.getAllCourses().subscribe({
      next: (data) => {
        this.courses = data || [];
        this.loading = false;

        if (!this.selectedCourseId && this.courses.length > 0) {
          this.selectedCourseId = this.courses[0].id ?? null;
        }
      },
      error: (err) => {
        console.error(err);
        this.error = 'Failed to load courses.';
        this.loading = false;
      }
    });
  }

  onCourseChange(): void {
    // nothing needed; LessonsList reloads via @Input changes
    // reset attachment panel selection because course changed
    this.selectedLessonId = null;
    this.attachments = [];
    this.selectedFile = null;
    this.message = null;
    this.error = null;
  }

  // ==========================
  // ✅ Attachments admin logic
  // ==========================
loadAttachments(): void {
  if (!this.selectedLessonId) {
    this.error = 'Veuillez saisir un ID de leçon.';
    return;
  }

  this.loading = true;
  this.error = null;

  this.lessonsService.getLessonAttachments(this.selectedLessonId).subscribe({
    next: (data) => {
      this.attachments = data || [];
      this.loading = false;
    },
    error: () => {
      this.error = 'Impossible de charger les fichiers.';
      this.loading = false;
    }
  });
}

  onFileSelected(event: any): void {
    const file = event?.target?.files?.[0];
    this.selectedFile = file ?? null;
    this.message = null;
    this.error = null;
  }

  uploadAttachment(): void {
    if (!this.selectedLessonId) {
      this.error = 'Veuillez saisir un ID de leçon.';
      return;
    }
    if (!this.selectedFile) {
      this.error = 'Veuillez choisir un fichier.';
      return;
    }

    this.loading = true;
    this.error = null;
    this.message = null;

    this.lessonsService.uploadLessonAttachment(this.selectedLessonId, this.selectedType, this.selectedFile).subscribe({
      next: () => {
        this.message = 'Fichier uploadé avec succès ✅';
        this.loading = false;
        this.selectedFile = null;

        // refresh list
        this.loadAttachments();
      },
      error: (err) => {
        console.error(err);
        this.error = err?.error?.message || 'Upload échoué.';
        this.loading = false;
      }
    });
  }

  attachmentUrl(id: number): string {
    return this.lessonsService.getAttachmentUrl(id);
  }
  loadProgress(courseId: number) {
  this.lessonsService.getCourseProgressSummary(courseId)
    .subscribe(summary => {
      this.progressSummary = summary;
    });
}
}