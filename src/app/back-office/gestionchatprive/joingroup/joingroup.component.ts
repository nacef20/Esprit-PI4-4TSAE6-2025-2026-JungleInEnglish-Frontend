import { Component, EventEmitter, OnInit, Output, Input } from '@angular/core';
import { JoinGroupService, JoinGroupDTO } from '../../../services/join-group.service';
import { KeycloakUserService } from '../../../services/keycloak-user.service';
@Component({
  selector: 'app-joingroup',
  templateUrl: './joingroup.component.html',
  styleUrl: './joingroup.component.scss'
})
export class JoingroupComponent {

demandes: JoinGroupDTO[] = [];
  @Output() close = new EventEmitter<void>();
 @Input() groupChatId!: number;
 userCache: { [key: string]: any } = {};
  constructor(private joinGroupService: JoinGroupService, private keycloakUserService: KeycloakUserService) { }

  ngOnInit(): void {
    this.loadDemandes();
  }

   loadDemandes(): void {
    this.joinGroupService.getAllByGroup(this.groupChatId).subscribe({
      next: (data) => {
        this.demandes = data;
        this.loadUserDetails();  
        
      },
      error: (err) => {
        console.error('Erreur lors du chargement des demandes', err);
      }
    });
  }

  // Fermer le modal
  fermerModal() {
    this.close.emit();
  }

accepterDemande(demande: JoinGroupDTO) {
  this.joinGroupService.accepterDemande(demande.idJoinGroup!).subscribe({
    next: () => {
      demande.status = 'ACCEPTE';
    },
    error: (err) => console.error('Erreur lors de l’acceptation', err)
  });
}

refuserDemande(demande: JoinGroupDTO) {
  this.joinGroupService.refuserDemande(demande.idJoinGroup!).subscribe({
    next: () => {
      demande.status = 'REFUSE';
    },
    error: (err) => console.error('Erreur lors du refus', err)
  });
}



loadUserDetails() {
  this.demandes.forEach(demande => {

    if (!this.userCache[demande.userId]) {

      this.keycloakUserService.getUserById(demande.userId).subscribe({
        next: (user) => {

          this.userCache[demande.userId] = user;

          this.demandes = this.demandes.map(m =>
            m.userId === demande.userId
              ? { ...m, firstName: user.firstName, lastName: user.lastName }
              : m
          );
          console.log(user)

        },
        error: (err) => console.error('Error loading user', err)
      });

    } else {
      // si déjà en cache
      const cachedUser = this.userCache[demande.userId];
      demande.firstName = cachedUser.firstName;
      demande.lastName = cachedUser.lastName;
    }

  });
}



}
