import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MemberGroupService, GroupMemberDTO } from '../../../services/member-group.service';
import { KeycloakUserService } from '../../../services/keycloak-user.service';
import { GroupService } from '../../../services/group.service';
import { AuthService } from '../../../auth/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-membregroup',
  templateUrl: './membregroup.component.html',
  styleUrl: './membregroup.component.scss'
})
export class MembregroupComponent {

@Input() groupId!: number;
  @Output() close = new EventEmitter<void>();

  members: GroupMemberDTO[] = [];
userCache: { [key: string]: any } = {};
groupAdmin: any;
  currentUserId: any = null; 
  currentUser: any = null;
  constructor(private memberGroupService: MemberGroupService, private keycloakUserService: KeycloakUserService
    , private groupService: GroupService, private authService: AuthService) {}

  ngOnInit(): void {

     this.currentUser = this.authService.getUserInfo();
    this.currentUserId = this.currentUser?.id;
    // Dès que le composant est créé (*ngIf=true), on charge les membres
    this.memberGroupService.getMembersByGroup(this.groupId).subscribe({
      next: (data) => {
        this.members = data;
        this.loadUserDetails();
      },
      error: (err) => console.error('Error loading members', err)
    });
    this.groupService.getGroupById(this.groupId).subscribe({
    next: (group) => {

      const ownerId = group.ownerId;

     
      this.keycloakUserService.getUserById(ownerId).subscribe({
        next: (user) => {
          this.groupAdmin = user;
        },
        error: (err) => console.error('Error loading admin user', err)
      });

    },
    error: (err) => console.error('Error loading group', err)
  });
  }

 loadUserDetails() {
  this.members.forEach(member => {

    if (!this.userCache[member.userId]) {

      this.keycloakUserService.getUserById(member.userId).subscribe({
        next: (user) => {

          this.userCache[member.userId] = user;

          this.members = this.members.map(m =>
            m.userId === member.userId
              ? { ...m, firstName: user.firstName, lastName: user.lastName }
              : m
          );
          console.log(user)

        },
        error: (err) => console.error('Error loading user', err)
      });

    } else {
      // si déjà en cache
      const cachedUser = this.userCache[member.userId];
      member.firstName = cachedUser.firstName;
      member.lastName = cachedUser.lastName;
    }

  });
}


  closeModal() {
    this.members = [];
    this.close.emit();
  }

removeMember(member: GroupMemberDTO) {
  // Vérifie si c'est l'admin
  if (member.userId === this.groupAdmin.userId) {
    Swal.fire({
      icon: 'warning',
      title: 'Attention',
      text: "Vous ne pouvez pas supprimer l'admin du groupe !"
    });
    return;
  }

  // Demande confirmation avant suppression
  Swal.fire({
    icon: 'warning',
    title: 'Do you want to remove this member ?',
    
    showCancelButton: true,
    confirmButtonText: 'yes, remove',
    cancelButtonText: 'no, cancel'
  }).then((result) => {
    if (result.isConfirmed) {
      // Si l'utilisateur confirme, on appelle le service
      this.memberGroupService.removeMember(this.groupId, member.userId)
        .subscribe({
          next: () => {
            // Mettre à jour la liste
            this.members = this.members.filter(m => m.userId !== member.userId);

            // Notification succès
            Swal.fire({
              icon: 'success',
              title: 'Membre supprimé !',
              text: 'the member has been removed successfully.',
              timer: 3000,
              showConfirmButton: false
            });

            
          },
          error: (err) => {
            Swal.fire({
              icon: 'error',
              title: 'Erreur',
              text: "Erreur lors de la suppression du membre !"
            });
            console.error("Erreur lors de la suppression du membre", err);
          }
        });
    } else {
      // Si l'utilisateur annule
      console.log("Suppression annulée");
    }
  });
}


}
