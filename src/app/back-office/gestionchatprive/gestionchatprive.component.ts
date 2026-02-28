import { GroupService } from '../../services/group.service';
import { MessageGroupService } from '../../services/message-group.service';
import { JoinGroupService ,JoinGroupDTO } from '../../services/join-group.service';
import { Component, HostListener } from '@angular/core'; 
import { GroupData } from './create-group/create-group.component';

interface Group {
  id: number;
  name: string;
  avatar: string;
  lastMessage: string;
  time: string;
  unread: number;
  online: number;
  ownerId?: number; 
}

interface Message {
  id: number;
  sender: number;
  content: string;
  groupChatId: number;
  time: string;
   showDelete?: boolean;  
  deleted?: boolean;    
}

@Component({
  selector: 'app-gestionchatprive',
  templateUrl: './gestionchatprive.component.html',
  styleUrl: './gestionchatprive.component.scss'
})
export class GestionchatpriveComponent {

  showAddGroupModal: boolean = false;
  openMenuId: number | null = null;
  isEditMode: boolean = false;
  selectedGroupToEdit: GroupData | null = null;  
  currentUserId: number = 1; 
  groups: Group[] = [];
  messages: Message[] = [];
  statusMap: { [groupChatId: number]: string | null } = {};
  emojis: any[] = [];   
  showEmojis = false;   

  selectedGroup: Group | null = null;
  searchQuery: string = '';
  newMessage: string = '';

  constructor(private groupService: GroupService, private messageGroupService: MessageGroupService,
    private joinGroupService: JoinGroupService
  ) { }

ngOnInit(): void {
  this.groupService.getAllGroups().subscribe((data: any[]) => {
    this.groups = data.map(g => ({
      id: g.id,
      name: g.name,
      avatar: g.image || '',
      lastMessage: g.lastMessage || 'Aucun message',
      time: g.time || '',
      unread: g.unread || 0,
      online: g.online || 0,
      ownerId: g.ownerId,
    }));

    if (this.groups.length > 0) {
      this.selectedGroup = this.groups[0];
    }

    // ← ICI DEDANS, après que les groupes sont chargés
    this.groups.forEach(group => {
      this.getStatusForGroup(group.id);
    });
  });
}

  getStatusForGroup(groupChatId: number): void {
  this.joinGroupService.getStatus(this.currentUserId, groupChatId).subscribe({
    next: (status) => {
      this.statusMap[groupChatId] = status;
    },
    error: (err) => {
      if (err.status === 204) {
        this.statusMap[groupChatId] = null; 
      }
    }
  });
}

  // =============================================
  // GESTION DU MODAL
  // =============================================

  // Ouvrir modal en mode CRÉATION
  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedGroupToEdit = null;
    this.showAddGroupModal = true;
  }

  // Callback du modal (création OU modification)
  handleCreateGroup(data: GroupData): void {
    if (this.isEditMode && data.id) {
      // MODE MODIFICATION
      this.updateGroupInList(data);
    } else {
      // MODE CRÉATION
      this.addGroupToList(data);
    }
    
    // Fermer le modal et réinitialiser
    this.showAddGroupModal = false;
    this.isEditMode = false;
    this.selectedGroupToEdit = null;
    window.location.reload();
  }

  // Ajouter un nouveau groupe dans la liste
  private addGroupToList(data: GroupData): void {
    const newGroup: Group = {
      id: data.id || this.groups.length + 1,
      name: data.name,
      avatar: data.image || '',
      lastMessage: 'Nouveau groupe créé',
      time: 'Maintenant',
      unread: 0,
      online: 1
    };
    this.groups.unshift(newGroup);
    this.selectGroup(newGroup);
  }

  // Mettre à jour un groupe existant dans la liste
  private updateGroupInList(data: GroupData): void {
    const index = this.groups.findIndex(g => g.id === data.id);
    
    if (index !== -1) {
      this.groups[index] = {
        ...this.groups[index],
        name: data.name,
        avatar: data.image
      };
      
      // Si c'est le groupe sélectionné, le mettre à jour aussi
      if (this.selectedGroup?.id === data.id) {
        this.selectedGroup = this.groups[index];
      }
    }
  }

  // =============================================
  // ACTIONS DU MENU 3 POINTS
  // =============================================

  toggleMenu(groupId: number): void {
    this.openMenuId = this.openMenuId === groupId ? null : groupId;
  }

joinGroup(group: Group): void {
  const dto = {
    groupChatId: group.id,
    userId: this.currentUserId
  };

  this.joinGroupService.addRequest(dto).subscribe({
    next: (res) => {
      console.log('Demande envoyée:', res);
      this.statusMap[group.id] = 'ATTENTE'; 
    },
    error: (err) => {
      console.error('Erreur en envoyant la demande:', err);
    }
  });

  this.openMenuId = null;
}


  editGroup(group: Group): void {
    this.isEditMode = true;
    this.showAddGroupModal = true;
    this.openMenuId = null;

    this.selectedGroupToEdit = {
      id: group.id,
      name: group.name,
      image: group.avatar,
      ownerId: this.currentUserId  
    };
  }

  deleteGroup(group: Group): void {
    const confirmed = confirm(
      `Voulez-vous vraiment supprimer "${group.name}" ?`
    );
    
    if (confirmed) {
      this.groupService.deleteGroup(group.id).subscribe({
        next: () => {
          // Retirer de la liste
          this.groups = this.groups.filter(g => g.id !== group.id);
          
          // Désélectionner si c'était le groupe actif
          if (this.selectedGroup?.id === group.id) {
            this.selectedGroup = this.groups.length > 0 ? this.groups[0] : null;
          }
          
          console.log('Groupe supprimé avec succès');
        },
        error: (err) => {
          console.error('Erreur suppression:', err);
          alert('Erreur lors de la suppression du groupe');
        }
      });
    }
    
    this.openMenuId = null;
  }

  @HostListener('document:click', ['$event'])
  closeMenuOnClickOutside(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown-menu')) {
      this.openMenuId = null;
    }
  }

  // =============================================
  // AUTRES MÉTHODES
  // =============================================

  get filteredGroups(): Group[] {
    if (!this.searchQuery) return this.groups;
    return this.groups.filter(g => 
      g.name.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

selectGroup(group: Group): void {
  this.selectedGroup = group;
  group.unread = 0;

this.messageGroupService.getMessagesByGroupId(group.id).subscribe({
  next: (messages: any[]) => {
    this.messages = messages.map(msg => ({
      id: msg.idMessageChat || 0,
      sender: msg.sender,
      content: msg.content,
       deleted: msg.deleted ,
      groupChatId: msg.groupChat?.id ?? group.id, // fallback si backend ne renvoie pas groupChat
      time: msg.dateSend
        ? new Date(msg.dateSend).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
        : '' 
    }));
    setTimeout(() => this.scrollToBottom(), 100);
  },
  error: (err) => console.error('Erreur chargement messages', err)
});

}



  sendMessage(): void {
  if (!this.newMessage.trim() || !this.selectedGroup) return;

  const messageObj = {
    sender: this.currentUserId,
    content: this.newMessage,
    groupChatId: this.selectedGroup.id 
  };

  this.messageGroupService.addMessage(messageObj).subscribe({
    next: (response: any) => {
      // Utiliser l'id réel du backend
      this.messages.push({
        id: response.idMessageChat,  // ← ID réel
        sender: response.sender,
        content: response.content,
        time: new Date(response.dateSend).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),
        groupChatId: response.groupChatId,
        deleted: response.deleted
      });
      this.newMessage = '';
      setTimeout(() => this.scrollToBottom(), 100);
    },
    error: (err) => console.error('Erreur lors de l’envoi du message', err)
  });
}


deleteMessage(msg: Message): void {
  this.messageGroupService.softDeleteMessage(msg.id).subscribe({
    next: () => {
      msg.deleted = true;          // marque côté frontend
      msg.showDelete = false;
      msg.content = "Message supprimé";  // modifie l’affichage
      console.log("Message soft supprimé côté frontend");
    },
    error: (err) => console.error('Erreur lors de la suppression', err)
  });
}




showDeleteIcon(msg: Message): void {
  // cacher toutes les autres icônes
  this.messages.forEach(m => m.showDelete = false);

  // afficher seulement celle du message cliqué
  if (msg.sender === this.currentUserId && !msg.deleted) {
    msg.showDelete = true;
  }

  // cacher après 3 secondes
  setTimeout(() => {
    msg.showDelete = false;
  }, 3000);
}

 
  startVideoCall(): void {
    alert('Video call in progress...');
  }

  startAudioCall(): void {
    alert('Audio call in progress...');
  }

  private scrollToBottom(): void {
    const messagesContainer = document.querySelector('.messages-container');
    if (messagesContainer) {
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
  }


    

 

loadEmojis() {
  this.showEmojis = !this.showEmojis;

  if (this.emojis.length > 0) return;

  this.messageGroupService.loadEmojis().subscribe({
    next: (data) => {
      console.log('Emojis reçus :', data); // <--- voir la structure
      this.emojis = data.slice(0, 50);
    },
    error: (err) => console.error('Erreur API emoji', err)
  });
}

addEmoji(emoji: string) {
  // si c’est un htmlCode comme &#128512;, on convertit en vrai emoji
  if (emoji.startsWith('&#')) {
    const temp = document.createElement('textarea');
    temp.innerHTML = emoji;
    emoji = temp.value;
  }

  this.newMessage += emoji;
  this.showEmojis = false;
}



  
}