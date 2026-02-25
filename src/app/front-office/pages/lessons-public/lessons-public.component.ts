import { Component, Input, OnChanges, OnDestroy } from '@angular/core';
import { Subscription, interval } from 'rxjs';
import { LessonsService } from '../../../services/lessons.service';
import { Lesson } from '../../../models/lesson.model';
import { ProgressService } from '../../../services/progress.service';

@Component({
  selector: 'app-lessons-public',
  templateUrl: './lessons-public.component.html',
  styleUrls: ['./lessons-public.component.scss']
})
export class LessonsPublicComponent implements OnChanges, OnDestroy {
  @Input() courseId!: number;

  lessons: Lesson[] = [];
  loading = false;
  error: string | null = null;

  showLessonViewer = false;
  selectedLesson: Lesson | null = null;

  // ✅ heartbeat fields
  private hbSub?: Subscription;
  private lastTickMs = Date.now();

  constructor(
    private lessonsService: LessonsService,
    private progressService: ProgressService
  ) {}

  ngOnChanges(): void {
    if (this.courseId != null) this.loadLessons();
  }

  ngOnDestroy(): void {
    this.stopHeartbeat();
  }

  loadLessons(): void {
    this.loading = true;
    this.error = null;

    this.lessonsService.getAllLessons().subscribe({
      next: (data) => {
        this.lessons = (data || [])
          .filter(l => l.courseId === this.courseId)
          .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0));
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Failed to load lessons.';
        this.loading = false;
      }
    });
  }

  openLesson(lesson: Lesson): void {
    this.selectedLesson = lesson;
    this.showLessonViewer = true;

    // ✅ start heartbeat when lesson viewer opens
    if (lesson.id != null) {
      this.stopHeartbeat(); // if another lesson already running
      this.startHeartbeat(lesson.id, lesson.courseId);
    }
  }

  closeLesson(): void {
    // ✅ stop heartbeat when viewer closes
    this.stopHeartbeat();

    this.showLessonViewer = false;
    this.selectedLesson = null;
  }

  normalizeVideoUrl(url?: string): string | null {
    if (!url) return null;
    return url.trim();
  }

  // -----------------------
  // ✅ Learner key (your version)
  // -----------------------
  getLearnerKey(): string {
    const keyName = 'learnerKey';
    const existing = localStorage.getItem(keyName);
    if (existing) return existing;

    const generated =
      (typeof crypto !== 'undefined' && (crypto as any).randomUUID)
        ? (crypto as any).randomUUID()
        : `${Date.now()}-${Math.random().toString(16).slice(2)}`;

    localStorage.setItem(keyName, generated);
    return generated;
  }

  // -----------------------
  // ✅ Heartbeat core logic
  // -----------------------
  private startHeartbeat(lessonId: number, courseId: number): void {
    const learnerKey = this.getLearnerKey();
    this.lastTickMs = Date.now();

    this.hbSub = interval(15000).subscribe(() => {
      // don’t count time when tab not visible
      if (document.hidden) return;

      const now = Date.now();
      const deltaSeconds = Math.max(1, Math.round((now - this.lastTickMs) / 1000));
      this.lastTickMs = now;

      this.progressService.heartbeatLesson(lessonId, {
        learnerKey,
        courseId,
        deltaSeconds
      }).subscribe({
        error: (err) => console.error('Heartbeat failed', err)
      });
    });
  }

  private stopHeartbeat(): void {
    this.hbSub?.unsubscribe();
    this.hbSub = undefined;
  }
}