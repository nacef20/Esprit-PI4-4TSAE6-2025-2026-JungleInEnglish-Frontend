import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { QuizService, AiQuizRequest } from '../../services/quiz.service';

@Component({
  selector: 'app-ai-quiz-generator',
  templateUrl: './ai-quiz-generator.component.html',
  styleUrls: ['./ai-quiz-generator.component.scss']
})
export class AiQuizGeneratorComponent {

  prompt = '';
  numberOfQuestions = 5;
  difficulty = 'medium';
  selectedFile: File | null = null;
  fileName = '';
  filePreview: string | null = null;
  isDragging = false;

  loading = false;
  error = '';
  success = '';
  generatedQuizId: number | null = null;

  constructor(
    private quizService: QuizService,
    private router: Router
  ) {}

  // ── File Handling ──

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.setFile(input.files[0]);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.setFile(event.dataTransfer.files[0]);
    }
  }

  private setFile(file: File): void {
    this.selectedFile = file;
    this.fileName = file.name;
    this.error = '';

    // Preview for images
    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = () => this.filePreview = reader.result as string;
      reader.readAsDataURL(file);
    } else {
      this.filePreview = null;
    }
  }

  removeFile(): void {
    this.selectedFile = null;
    this.fileName = '';
    this.filePreview = null;
  }

  getFileIcon(): string {
    if (!this.selectedFile) return 'upload_file';
    if (this.selectedFile.type === 'application/pdf') return 'picture_as_pdf';
    if (this.selectedFile.type.startsWith('image/')) return 'image';
    return 'upload_file';
  }

  getFileSize(): string {
    if (!this.selectedFile) return '';
    const bytes = this.selectedFile.size;
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  // ── Generation ──

  canGenerate(): boolean {
    return !this.loading;
  }

  generate(): void {
    if (this.loading) return;

    this.loading = true;
    this.error = '';
    this.success = '';
    this.generatedQuizId = null;

    const request: AiQuizRequest = {
      prompt: this.prompt.trim() || undefined,
      numberOfQuestions: this.numberOfQuestions,
      difficulty: this.difficulty
    };

    this.quizService.generateWithAi(this.selectedFile, request).subscribe({
      next: (quiz) => {
        this.loading = false;
        this.generatedQuizId = quiz.id!;
        this.success = `Quiz "${quiz.title}" created successfully with ${this.numberOfQuestions} questions!`;
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.error || err.message || 'An unexpected error occurred.';
        this.error = msg;
      }
    });
  }

  goToQuiz(): void {
    if (this.generatedQuizId) {
      this.router.navigate(['/back/quizzes/edit', this.generatedQuizId]);
    }
  }

  goToQuestions(): void {
    if (this.generatedQuizId) {
      this.router.navigate(['/back/quizzes', this.generatedQuizId, 'questions']);
    }
  }

  goToQuizList(): void {
    this.router.navigate(['/back/quizzes']);
  }

  reset(): void {
    this.prompt = '';
    this.numberOfQuestions = 5;
    this.difficulty = 'medium';
    this.selectedFile = null;
    this.fileName = '';
    this.filePreview = null;
    this.error = '';
    this.success = '';
    this.generatedQuizId = null;
  }
}
