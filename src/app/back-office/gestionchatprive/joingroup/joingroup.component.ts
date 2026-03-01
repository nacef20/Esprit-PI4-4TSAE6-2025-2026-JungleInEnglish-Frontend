import { Component, EventEmitter, OnInit, Output, Input } from '@angular/core';
import { JoinGroupService, JoinGroupDTO } from '../../../services/join-group.service';
@Component({
  selector: 'app-joingroup',
  templateUrl: './joingroup.component.html',
  styleUrl: './joingroup.component.scss'
})
export class JoingroupComponent {

demandes: JoinGroupDTO[] = [];
  @Output() close = new EventEmitter<void>();
 @Input() groupChatId!: number;
  constructor(private joinGroupService: JoinGroupService) { }

  ngOnInit(): void {
    this.loadDemandes();
  }

   loadDemandes(): void {
    this.joinGroupService.getAllByGroup(this.groupChatId).subscribe({
      next: (data) => {
        this.demandes = data;
        console.log('Demandes:', this.demandes);
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




}
