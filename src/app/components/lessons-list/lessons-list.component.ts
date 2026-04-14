import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Lesson } from '../../models/lesson.model';
import { CourseProgressSummary, LessonsService } from '../../services/lessons.service';
import { LessonAttachment, AttachmentType } from '../../models/lesson-attachment.model';
import { CoursesService } from '../../services/courses.service';
import { Course } from '../../models/course.model';

@Component({
  selector: 'app-lessons-list',
  templateUrl: './lessons-list.component.html',
  styleUrls: ['./lessons-list.component.scss']
})
export class LessonsListComponent implements OnChanges {
  @Input() courseId!: number;
  @Input() adminMode: boolean = false;
  course: Course | null = null;
progressSummary!: CourseProgressSummary;
  lessons: Lesson[] = [];
  lessonsLoading = false;
  error: string | null = null;

  showLessonViewer = false;
  selectedLesson: Lesson | null = null;

  showLessonModal = false;
  isEditMode = false;
  saving = false;

  currentLesson: Lesson = this.emptyLesson();

  // ✅ Attachments state (shared for viewer + admin modal)
  attachments: LessonAttachment[] = [];
  attachmentsLoading = false;
  attachmentsError: string | null = null;

  selectedType: AttachmentType = 'PDF';
  selectedFile: File | null = null;
  uploadMessage: string | null = null;
  uploadError: string | null = null;
  uploading = false;

  toastMessage: string | null = null;
  toastType: 'success' | 'error' = 'success';
  private toastTimer?: any;

  // progress tracking (for public viewer)
unlockedOrderIndexMax = 1; // default: first lesson unlocked
courseProgressPercent = 0;

lessonDifficulty = new Map<number, {
  difficulty: 'HARD' | 'MEDIUM' | 'EASY';
  completionRate: number;
  avgTimeSeconds: number;
}>();
difficultyLoading = false;

  // to prevent multiple “complete” calls
  private completedLessonSent = new Set<number>();

  // time tracking for lesson
  private lessonStartTimestamp: number | null = null;
  constructor(
    private lessonsService: LessonsService,
    private coursesService: CoursesService
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['courseId'] && this.courseId) {
      this.loadCourse();
      this.loadLessons();
    }
  }

  loadCourse(): void {
    this.coursesService.getCourse(this.courseId).subscribe({
      next: (course) => this.course = course,
      error: (err) => console.error('Error loading course:', err)
    });
  }

loadLessons(): void {
  this.lessonsLoading = true;
  this.error = null;

  this.lessonsService.getLessonsByCourse(this.courseId).subscribe({
    next: (data) => {
      this.lessons = (data || []).sort(
        (a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0)
      );
      this.lessonsLoading = false;

      // progress summary
      this.lessonsService.getCourseProgressSummary(this.courseId).subscribe({
        next: (summary) => {
          this.courseProgressPercent = summary.completionPercent || 0;
          this.unlockedOrderIndexMax = summary.unlockedOrderIndexMax || 1;
        },
        error: () => {
          this.courseProgressPercent = 0;
          this.unlockedOrderIndexMax = 1;
        }
      });

      // ✅ difficulty analytics
      this.difficultyLoading = true;
      this.lessonDifficulty.clear();

      this.lessonsService.getLessonDifficulty(this.courseId).subscribe({
        next: (rows) => {
          (rows || []).forEach(r => {
            this.lessonDifficulty.set(r.lessonId, {
              difficulty: r.difficulty,
              completionRate: r.completionRate ?? 0,
              avgTimeSeconds: r.avgTimeSeconds ?? 0
            });
          });
          this.difficultyLoading = false;
        },
        error: () => {
          this.difficultyLoading = false;
        }
      });
    },
    error: (err) => {
      console.error('Error loading lessons:', err);
      this.error = err?.error?.message || 'Failed to load lessons for this course.';
      this.lessons = [];
      this.lessonsLoading = false;
      this.difficultyLoading = false;
    }
  });
}
getDifficultyLabel(lessonId?: number): 'HARD' | 'MEDIUM' | 'EASY' | null {
  if (!lessonId) return null;
return this.lessonDifficulty.get(lessonId)?.difficulty ?? null;
}

getDifficultyClass(label: string | null): string {
  if (label === 'HARD') return 'diff-hard';
  if (label === 'MEDIUM') return 'diff-medium';
  if (label === 'EASY') return 'diff-easy';
  return '';
}
isLessonLocked(lesson: Lesson): boolean {
  if (!this.progressSummary) return true;

  return lesson.orderIndex > this.progressSummary.unlockedOrderIndexMax;
}
  // =========================
  // VIEW LESSON (modal)
  // =========================
openLesson(lesson: Lesson): void {
  console.log('openLesson fired with:', lesson);

  if (!lesson.id) return;

  // open modal immediately
  this.selectedLesson = lesson;
  this.showLessonViewer = true;

  // start lesson progress
  this.lessonsService.startLessonProgress(lesson.id).subscribe({
    next: () => {},
    error: (err) => console.error('startLessonProgress failed', err)
  });

  // reset + load attachments
  this.resetAttachmentsUI();
  this.loadAttachments(lesson.id);

  // load full lesson details
  this.lessonsService.getLesson(lesson.id).subscribe({
    next: (full) => {
      this.selectedLesson = full;
    },
    error: (e) => {
      console.error('getLesson failed', e);
    }
  });
}
markLessonCompleted(lessonId: number): void {
  if (this.completedLessonSent.has(lessonId)) return;
  this.completedLessonSent.add(lessonId);

  const timeSpentSeconds =
    this.lessonStartTimestamp ? Math.floor((Date.now() - this.lessonStartTimestamp) / 1000) : undefined;

  this.lessonsService.completeLessonProgress(lessonId, timeSpentSeconds).subscribe({
    next: () => {
      this.lessonsService.getCourseProgressSummary(this.courseId).subscribe({
        next: (summary) => {
          this.courseProgressPercent = summary.completionPercent || 0;
          this.unlockedOrderIndexMax = summary.unlockedOrderIndexMax || this.unlockedOrderIndexMax;
        }
      });
    },
    error: () => {}
  });
}
onAttachmentClicked(attachmentId: number, lessonId: number): void {
  this.markAttachmentDone(attachmentId);

  // ✅ also consider the lesson completed (for file-based lessons)
  this.markLessonCompleted(lessonId);
}
onVideoTimeUpdate(event: Event, lessonId: number): void {
  const video = event.target as HTMLVideoElement;
  if (!video || !video.duration) return;

  if (video.currentTime >= video.duration - 1) {
    this.markLessonCompleted(lessonId);
  }
}

onVideoEnded(lessonId: number): void {
  this.markLessonCompleted(lessonId);
}
markAttachmentDone(attachmentId: number): void {
  this.lessonsService.completeAttachmentProgress(attachmentId).subscribe({ next: () => {}, error: () => {} });
}
closeLesson(): void {
  this.showLessonViewer = false;
  this.selectedLesson = null;

  if (this.videoObjectUrl) {
    URL.revokeObjectURL(this.videoObjectUrl);
    this.videoObjectUrl = null;
  }

  this.resetAttachmentsUI();
}

  // =========================
  // CREATE / EDIT LESSON (admin modal)
  // =========================
  openCreate(): void {
    this.isEditMode = false;
    this.currentLesson = this.emptyLesson();
    this.currentLesson.courseId = this.courseId;
    this.currentLesson.orderIndex = this.lessons.length + 1;

    this.resetAttachmentsUI(); // no attachments until saved
    this.showLessonModal = true;
  }

  openEdit(lesson: Lesson): void {
    this.isEditMode = true;
    this.currentLesson = { ...lesson };

    this.resetAttachmentsUI();
    if (lesson.id) {
      this.loadAttachments(lesson.id);
    }
    this.showLessonModal = true;
  }

  closeModal(): void {
    this.showLessonModal = false;
    this.resetAttachmentsUI();
  }

  save(): void {
    this.saving = true;
    this.error = null;

    const payload: Lesson = { ...this.currentLesson, courseId: this.courseId };

    const req$ =
      this.isEditMode && payload.id
        ? this.lessonsService.updateLesson(payload.id, payload)
        : this.lessonsService.createLesson(payload);

    req$.subscribe({
      next: (savedLesson: any) => {
        this.saving = false;

        if (!this.isEditMode) {
          this.showLessonModal = false;
          this.showToast('Lesson added ✅', 'success');
          this.loadLessons();
          return;
        }

        // ✅ close after update too
        this.showLessonModal = false;
        this.showToast('Lesson updated ✅', 'success');
        this.loadLessons();
      },
      error: (err) => {
        console.error('Error saving lesson:', err);

const msg =
  err?.error?.message ||
  err?.error?.error ||
  err?.message ||
  'Failed to save lesson.';

this.error = msg;
this.showToast(msg, 'error');
      }
    });
  }

  delete(lesson: Lesson): void {
    if (!lesson.id) return;
    const ok = confirm(`Delete lesson "${lesson.title}"?`);
    if (!ok) return;

    this.lessonsService.deleteLesson(lesson.id).subscribe({
      next: () => this.loadLessons(),
      error: (err) => {
        console.error('Error deleting lesson:', err);
        this.error = 'Failed to delete lesson.';
      }
    });
  }

  // =========================
  // ATTACHMENTS (admin + view)
  // =========================
loadAttachments(lessonId: number): void {
  this.attachmentsLoading = true;
  this.attachmentsError = null;

  this.lessonsService.getLessonAttachments(lessonId).subscribe({
    next: (data) => {
      this.attachments = data || [];
      this.attachmentsLoading = false;

      const video = this.attachments.find(a => a.type === 'VIDEO');
      if (video?.id) {
        this.loadVideoAttachment(video.id);
      } else {
        if (this.videoObjectUrl) {
          URL.revokeObjectURL(this.videoObjectUrl);
          this.videoObjectUrl = null;
        }
      }
    },
    error: (err) => {
      console.error(err);
      this.attachments = [];
      this.attachmentsError = 'Failed to load attachments.';
      this.attachmentsLoading = false;
    }
  });
}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files.length > 0 ? input.files[0] : null;
    this.uploadMessage = null;
    this.uploadError = null;
  }

  uploadAttachmentForCurrentLesson(): void {
    const lessonId = this.currentLesson?.id;

    this.uploadMessage = null;
    this.uploadError = null;

    if (!lessonId) {
      this.uploadError = 'Save the lesson first, then upload files.';
      return;
    }

    if (!this.selectedFile) {
      this.uploadError = 'Please choose a file.';
      return;
    }

if (!this.isTypeMatchingFile(this.selectedType, this.selectedFile)) {
  this.uploadError = 'Select the correct type for the uploaded file.';
  return;
}

    this.uploading = true;

    this.lessonsService
      .uploadLessonAttachment(lessonId, this.selectedType, this.selectedFile)
      .subscribe({
        next: () => {
          this.uploading = false;
          this.uploadMessage = 'Uploaded ✅';
          this.selectedFile = null;
          this.loadAttachments(lessonId);
        },
        error: (err) => {
          console.error(err);
          this.uploading = false;
          this.uploadError = err?.error?.message || 'Upload failed.';
        }
      });
  }

videoObjectUrl: string | null = null;

openAttachment(attachmentId: number): void {
  this.lessonsService.getAttachmentBlob(attachmentId).subscribe({
    next: (blob) => {
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank');
    },
    error: (err) => {
      console.error('Error opening attachment:', err);
      this.showToast('Failed to open attachment ❌', 'error');
    }
  });
}

loadVideoAttachment(attachmentId: number): void {
  if (this.videoObjectUrl) {
    URL.revokeObjectURL(this.videoObjectUrl);
    this.videoObjectUrl = null;
  }

  this.lessonsService.getAttachmentBlob(attachmentId).subscribe({
    next: (blob) => {
      this.videoObjectUrl = URL.createObjectURL(blob);
    },
    error: (err) => {
      console.error('Error loading video attachment:', err);
      this.showToast('Failed to load video ❌', 'error');
    }
  });
}
  get uploadedVideo(): LessonAttachment | undefined {
    return this.attachments.find(a => a.type === 'VIDEO');
  }

  get pdfFiles(): LessonAttachment[] {
    return this.attachments.filter(a => a.type === 'PDF');
  }

  get otherFiles(): LessonAttachment[] {
    return this.attachments.filter(a => a.type !== 'VIDEO' && a.type !== 'PDF');
  }

  private isTypeMatchingFile(type: AttachmentType, file: File): boolean {
  if (!file) return false;

  const mimeType = (file.type || '').toLowerCase();
  const fileName = (file.name || '').toLowerCase();

  switch (type) {
    case 'PDF':
      return mimeType === 'application/pdf' || fileName.endsWith('.pdf');

    case 'WORD':
      return fileName.endsWith('.doc') ||
             fileName.endsWith('.docx') ||
             mimeType.includes('msword') ||
             mimeType.includes('officedocument.wordprocessingml');

    case 'PPT':
      return fileName.endsWith('.ppt') ||
             fileName.endsWith('.pptx') ||
             mimeType.includes('ms-powerpoint') ||
             mimeType.includes('officedocument.presentationml');

    case 'VIDEO':
      return mimeType.startsWith('video/') ||
             /\.(mp4|webm|ogg|mov|mkv)$/.test(fileName);

    case 'OTHER':
    default:
      return true;
  }
}

  private resetAttachmentsUI(): void {
    this.attachments = [];
    this.attachmentsLoading = false;
    this.attachmentsError = null;
    this.selectedType = 'PDF';
    this.selectedFile = null;
    this.uploadMessage = null;
    this.uploadError = null;
    this.uploading = false;
    if (this.videoObjectUrl) {
  URL.revokeObjectURL(this.videoObjectUrl);
  this.videoObjectUrl = null;
}
  }

  private emptyLesson(): Lesson {
    return {
      title: '',
      content: '',
      videoUrl: '',
      duration: '',
      orderIndex: 1,
      courseId: 0
    };
  }

  deleteAttachment(attachmentId: number): void {
    const ok = confirm('Delete this attachment?');
    if (!ok) return;

    this.lessonsService.deleteLessonAttachment(attachmentId).subscribe({
      next: () => {
        this.showToast('Attachment deleted ✅', 'success');
        if (this.currentLesson.id) this.loadAttachments(this.currentLesson.id);
      },
      error: () => this.showToast('Failed to delete attachment ❌', 'error')
    });
  }

  normalizeVideoUrl(url?: string): string | null {
    if (!url) return null;
    return url.trim();
  }

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = message;
    this.toastType = type;

    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => {
      this.toastMessage = null;
    }, 2500);
  }
  loadProgress(courseId: number) {
  this.lessonsService.getCourseProgressSummary(courseId)
    .subscribe(summary => {
      this.progressSummary = summary;
    });
}
isLocked(lesson: any): boolean {

  return lesson.orderIndex > this.unlockedOrderIndexMax;
}
loadCourseSummary() {
  this.lessonsService.getCourseProgressSummary(this.courseId).subscribe({
    next: (s) => {
            console.log("SUMMARY", s); // 👈 check this in browser console

      this.courseProgressPercent = s.completionPercent;
      this.unlockedOrderIndexMax = s.unlockedOrderIndexMax ?? 1;
    },
    error: () => {
      // keep default unlock=1 if summary fails
      this.unlockedOrderIndexMax = 1;
    }
  });
}
onLessonCompleted() {
  this.loadCourseSummary();
  this.loadLessons(); // optional if you want refresh
}
}
