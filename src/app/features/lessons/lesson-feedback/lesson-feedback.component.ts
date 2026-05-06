import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { FeedbackService } from '../../../services/feedback.service';
import { AuthService } from '../../../auth/auth.service';
import {
  FeedbackResponse,
  ProfessorBadgeResponse,
  ProfessorBadgeTier,
  ReactionType
} from '../../../models/feedback.model';

@Component({
  selector: 'app-lesson-feedback',
  standalone: false,
  templateUrl: './lesson-feedback.component.html',
  styleUrls: ['./lesson-feedback.component.scss']
})
export class LessonFeedbackComponent implements OnInit, OnChanges {
  @Input() lessonId!: number;
  @Input() courseId!: number;
  @Input() professorId!: string;   // Keycloak sub of the professor

  feedbacks: FeedbackResponse[] = [];
  professorBadge: ProfessorBadgeResponse | null = null;

  // Form state
  showForm = false;
  newComment = '';
  hoveredStar = 0;
  selectedStar = 0;
  submitting = false;
  formError: string | null = null;

  // List state
  loading = false;
  error: string | null = null;

  // Reaction loading guard (feedbackId → true while in-flight)
  reactionLoading: Record<number, boolean> = {};

  get currentStudentId(): string {
    return this.authService.getUserInfo()?.id ?? '';
  }

  get isStudent(): boolean {
    return this.authService.isStudent();
  }

  /** The feedback the current student already submitted for this lesson, if any. */
  get myFeedback(): FeedbackResponse | undefined {
    return this.feedbacks.find(f => f.studentId === this.currentStudentId);
  }

  constructor(
    private feedbackService: FeedbackService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['lessonId'] && !changes['lessonId'].firstChange) {
      this.reset();
      this.load();
    }
  }

  // ── Load ──────────────────────────────────────────────────────────────────

  load(): void {
    if (!this.lessonId) return;
    this.loading = true;
    this.error = null;

    this.feedbackService.getFeedbacksByLesson(this.lessonId).subscribe({
      next: (data) => {
        this.feedbacks = data ?? [];
        this.loading = false;
        this.loadBadge();
      },
      error: () => {
        this.error = 'Could not load feedback.';
        this.loading = false;
      }
    });
  }

  loadBadge(): void {
    if (!this.professorId) return;
    this.feedbackService.getProfessorBadge(this.professorId).subscribe({
      next: (b) => (this.professorBadge = b),
      error: () => { /* badge is optional — fail silently */ }
    });
  }

  // ── Submit feedback ───────────────────────────────────────────────────────

  openForm(): void {
    this.showForm = true;
    this.formError = null;
  }

  cancelForm(): void {
    this.showForm = false;
    this.newComment = '';
    this.selectedStar = 0;
    this.hoveredStar = 0;
    this.formError = null;
  }

  submitFeedback(): void {
    if (!this.selectedStar) {
      this.formError = 'Please select a star rating.';
      return;
    }
    if (!this.newComment.trim()) {
      this.formError = 'Please write a comment.';
      return;
    }

    this.submitting = true;
    this.formError = null;

    this.feedbackService.addFeedback({
      studentId: this.currentStudentId,
      lessonId: this.lessonId,
      courseId: this.courseId,
      professorId: this.professorId,
      comment: this.newComment.trim(),
      starRating: this.selectedStar
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.cancelForm();
        this.load();
      },
      error: (err) => {
        this.submitting = false;
        this.formError =
          err?.error?.message || err?.error?.error || 'Failed to submit feedback.';
      }
    });
  }

  // ── Delete own feedback ───────────────────────────────────────────────────

  deleteMyFeedback(): void {
    if (!this.myFeedback) return;
    if (!confirm('Delete your feedback?')) return;

    this.feedbackService.deleteFeedback(this.myFeedback.id, this.currentStudentId).subscribe({
      next: () => this.load(),
      error: () => alert('Could not delete feedback.')
    });
  }

  // ── Reactions ─────────────────────────────────────────────────────────────

  react(feedback: FeedbackResponse, type: ReactionType): void {
    if (feedback.studentId === this.currentStudentId) return; // can't react to own
    if (this.reactionLoading[feedback.id]) return;

    this.reactionLoading[feedback.id] = true;
    const payload = { studentId: this.currentStudentId, feedbackId: feedback.id, reactionType: type };

    const isSame = feedback.myReaction === type;
    const hadReaction = !!feedback.myReaction;

    const call$ = isSame
      ? this.feedbackService.removeReaction(feedback.id, this.currentStudentId)
      : hadReaction
        ? this.feedbackService.updateReaction(feedback.id, payload)
        : this.feedbackService.addReaction(feedback.id, payload);

    call$.subscribe({
      next: () => {
        this.reactionLoading[feedback.id] = false;
        // Optimistic: reload to get updated counts & score from server
        this.load();
      },
      error: () => {
        this.reactionLoading[feedback.id] = false;
      }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  stars(count: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i + 1);
  }

  hoverStar(n: number): void { this.hoveredStar = n; }
  leaveStar(): void         { this.hoveredStar = 0; }
  selectStar(n: number): void { this.selectedStar = n; }

  starFilled(n: number): boolean {
    return n <= (this.hoveredStar || this.selectedStar);
  }

  displayStarFilled(n: number, rating: number): boolean {
    return n <= rating;
  }

  qualityClass(score: number): string {
    if (score >= 5)  return 'quality--high';
    if (score >= 0)  return 'quality--neutral';
    return 'quality--low';
  }

  badgeLabel(tier: ProfessorBadgeTier): string {
    const labels: Record<ProfessorBadgeTier, string> = {
      NONE: '—', ROOKIE: '🌱 Rookie', BRONZE: '🥉 Bronze',
      SILVER: '🥈 Silver', GOLD: '🥇 Gold',
      PLATINUM: '💎 Platinum', LEGEND: '🏆 Legend'
    };
    return labels[tier] ?? tier;
  }

  badgeCssClass(tier: ProfessorBadgeTier): string {
    return `badge--${tier.toLowerCase()}`;
  }

  private reset(): void {
    this.feedbacks = [];
    this.professorBadge = null;
    this.showForm = false;
    this.newComment = '';
    this.selectedStar = 0;
    this.error = null;
  }
}