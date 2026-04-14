import { Component, EventEmitter, Output, Input, OnChanges, SimpleChanges } from '@angular/core';
import { GroupService } from '../../../services/group.service';
import { AuthService } from '../../../auth/auth.service';
export interface GroupData {
  id?: number;       // facultatif pour create
  name: string;
  image: string;
  ownerId?: string;  // facultatif
}

@Component({
  selector: 'app-create-group',
  templateUrl: './create-group.component.html',
  styleUrls: ['./create-group.component.scss']
})
export class CreateGroupComponent implements OnChanges {




  groupName: string = '';
  selectedFile?: File;
  previewUrl: string = '';
currentUser: any = null;
currentUserId: string | undefined = undefined;
  // ── Entrées et sorties ────────────────────
  @Input() editGroupData: GroupData | null = null;
  @Input() isEditMode: boolean = false;
  @Output() groupCreated = new EventEmitter<GroupData>();
  @Output() cancelled = new EventEmitter<void>();

  constructor(private groupService: GroupService ,private authService: AuthService) {}


  



  // ── Mise à jour du formulaire si édition ──
  ngOnChanges(changes: SimpleChanges): void {
 this.currentUser = this.authService.getUserInfo();
    this.currentUserId = this.currentUser?.id;
    console.log('Current User ID:', this.currentUserId);
    console.log('role:', this.currentUser?.roles);

    if (this.isEditMode && this.editGroupData) {
      this.groupName = this.editGroupData.name;
      this.previewUrl = 'http://localhost:8222/uploads/images/' + this.editGroupData.image;
    }
  }

  // ── Sélection d’une image ─────────────────
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      alert('Veuillez sélectionner une image (PNG, JPG, WEBP).');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert('L\'image est trop grande. Maximum 5 MB.');
      return;
    }

    this.selectedFile = file;

    const reader = new FileReader();
    reader.onload = () => {
      this.previewUrl = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  // ── Soumission du formulaire ─────────────
  submit(): void {
    if (!this.groupName.trim()) {
      alert('Le nom du groupe est obligatoire.');
      return;
    }

    if (this.isEditMode) {
      this.updateGroup();
    } else {
      this.createGroup();
    }
  }

  // ── Création d’un nouveau groupe ─────────
  private createGroup(): void {
    if (!this.selectedFile) {
      alert('Veuillez sélectionner une image.');
      return;
    }

    this.groupService.uploadImage(this.selectedFile).subscribe({
      next: (filename) => {
        const data: GroupData = {
          name: this.groupName.trim(),
          image: filename,
          ownerId: this.currentUserId
        };

        this.groupService.createGroup(data).subscribe({
          next: (res) => {
            this.groupCreated.emit(res);
            this.reset();
          },
          error: (err) => console.error(err)
        });
      },
      error: (err) => console.error(err)
    });
  }

  // ── Mise à jour d’un groupe existant ─────
  private updateGroup(): void {
    if (!this.editGroupData) return;

    // Si une nouvelle image est sélectionnée → upload
    if (this.selectedFile) {
      this.groupService.uploadImage(this.selectedFile).subscribe({
        next: (filename) => this.sendUpdate(filename)
      });
    } else {
      // Sinon conserver l’image existante
      this.sendUpdate(this.editGroupData.image);
    }
  }

  private sendUpdate(imageName: string): void {
    if (!this.editGroupData) return;

    const data: GroupData = {
      name: this.groupName.trim(),
      image: imageName,
      ownerId: this.editGroupData.ownerId
    };

    this.groupService.updateGroup(this.editGroupData.id!, data).subscribe({
      next: (res) => {
        this.groupCreated.emit(res);
        this.reset();
      },
      error: (err) => console.error(err)
    });
  }

  // ── Annuler / fermer modal ───────────────
  cancel(): void {
    this.reset();
    this.cancelled.emit();
  }

  // ── Réinitialiser le formulaire ──────────
  private reset(): void {
    this.groupName = '';
    this.selectedFile = undefined;
    this.previewUrl = '';
  }
}
