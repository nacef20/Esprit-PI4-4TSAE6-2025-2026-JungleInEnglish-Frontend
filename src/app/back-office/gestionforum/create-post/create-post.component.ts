import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService,PostDTO } from '../../../services/post.service';
import { AuthService } from '../../../auth/auth.service';


@Component({
  selector: 'app-create-post',
  templateUrl: './create-post.component.html',
  styleUrl: './create-post.component.scss'
})
export class CreatePostComponent {

 
  @Input() mediaType: 'photo' | 'video' | null = null;
  @Input() postToEdit: PostDTO | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() postCreated = new EventEmitter<void>();

  title = '';
  content = '';
  mediaPreview: string | null = null;
  mediaFile: File | null = null;
  currentUser: any = null;
  currentUserInitial = 'A';
  loading = false;
  error = '';

  constructor(
    private postService: PostService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getUserInfo();
    this.currentUser = user;
    this.currentUserInitial = user?.firstName?.[0]?.toUpperCase() ?? 'A';

    // Mode edit → pré-remplir
    if (this.postToEdit) {
      this.title = this.postToEdit.title ?? '';
      const c = this.postToEdit.content ?? '';
      if (this.isMediaUrl(c)) {
        this.mediaPreview = c;
      } else {
        this.content = c;
      }
    }
  }

  get modalTag(): string {
    return this.postToEdit ? 'MODIFIER POST' : 'NOUVEAU POST';
  }

  get modalHeading(): string {
    return this.postToEdit ? 'Modifier le post' : 'Créer un post';
  }

  get submitLabel(): string {
    return this.postToEdit ? 'Enregistrer' : 'Publier';
  }

  isMediaUrl(url: string): boolean {
    return url.startsWith('http') || url.startsWith('data:');
  }

  canSubmit(): boolean {
    return !!(this.title.trim() || this.content.trim() || this.mediaFile || this.mediaPreview) && !this.loading;
  }

  isImageFile(): boolean {
    if (this.mediaFile) return this.mediaFile.type.startsWith('image/');
    if (this.mediaPreview) return /\.(jpg|jpeg|png|gif|webp|svg)(\?.*)?$/i.test(this.mediaPreview);
    return false;
  }

  isVideoFile(): boolean {
    if (this.mediaFile) return this.mediaFile.type.startsWith('video/');
    if (this.mediaPreview) return /\.(mp4|webm|ogg|mov)(\?.*)?$/i.test(this.mediaPreview);
    return false;
  }

  onMediaSelected(e: Event): void {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.mediaFile = file;
    const reader = new FileReader();
    reader.onload = ev => this.mediaPreview = ev.target?.result as string;
    reader.readAsDataURL(file);
  }

  removeMedia(): void {
    this.mediaFile = null;
    this.mediaPreview = null;
  }

  cancel(): void { this.close.emit(); }

  onBackdropClick(e: MouseEvent): void {
    if ((e.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.cancel();
    }
  }

  submit(): void {
    if (!this.canSubmit()) return;
    this.loading = true;
    this.error = '';

    if (!this.mediaFile) {
      // Pas de nouveau fichier → contenu texte ou URL existante
      const contentValue = this.mediaPreview ?? this.content;
      this.savePost(contentValue);
      return;
    }

    // Nouveau fichier → uploader d'abord
    this.postService.uploadFile(this.mediaFile).subscribe(
      (filename: string) => {
        const mediaUrl = `http://localhost:9090/uploads/images/${filename}`;
        this.savePost(mediaUrl);
      },
      (err: any) => {
        this.loading = false;
        this.error = 'Erreur lors de l\'upload du fichier';
      }
    );
  }

  private savePost(contentValue: string): void {
    // Mode EDIT
    if (this.postToEdit?.idpost) {
      const updated: PostDTO = {
        ...this.postToEdit,
        title: this.title,
        content: contentValue
      };
      this.postService.updatePost(this.postToEdit.idpost, updated).subscribe({
        next: () => {
          this.loading = false;
          this.postCreated.emit();
          this.close.emit();
        },
        error: (err) => {
          this.loading = false;
          this.error = err?.error?.message || 'Erreur lors de la modification';
        }
      });
      return;
    }

    // Mode CREATE
    const post: PostDTO = {
      title: this.title,
      content: contentValue,
      authorid: this.currentUser?.id || 'unknown'
    };

    this.postService.createPost(post).subscribe({
      next: () => {
        this.loading = false;
        this.postCreated.emit();
        this.close.emit();
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Erreur lors de la création du post';
      }
    });
  }

}
