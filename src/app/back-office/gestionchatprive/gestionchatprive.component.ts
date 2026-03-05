import { GroupService } from '../../services/group.service';
import { MessageGroupService } from '../../services/message-group.service';
import { JoinGroupService ,JoinGroupDTO } from '../../services/join-group.service';
import { MemberGroupService } from '../../services/member-group.service';
import { Component, HostListener } from '@angular/core'; 
import { GroupData } from './create-group/create-group.component';
import { AuthService } from '../../auth/auth.service';

import { KeycloakUserService } from '../../services/keycloak-user.service';
import { BehaviorSubject } from 'rxjs';
import Swal from 'sweetalert2';

interface Group {
  id: number;
  name: string;
  avatar: string;
  lastMessage: string;
  time: string;
  unread: number;
  online: number;
  ownerId?: number; 
  isMember?: boolean;
}

interface Message {
  id: number;
  sender: number;
  content: string;
  groupChatId: number;
  time: string;
   showDelete?: boolean;  
  deleted?: boolean;    
   senderInfo?: any; 
}

interface MessageGroup {
  id: number;
  sender: number;
  content: string;
  groupChatId: number;
  time?: string;
  deleted?: boolean;
}

@Component({
  selector: 'app-gestionchatprive',
  templateUrl:'./gestionchatprive.component.html',
  styleUrl: './gestionchatprive.component.scss'
})
export class GestionchatpriveComponent {

  showAddGroupModal: boolean = false;
  openMenuId: number | null = null;
  isEditMode: boolean = false;
  selectedGroupToEdit: GroupData | null = null;  
  currentUserId: any = null; 
  groups: Group[] = [];
  messages: any[] = [];
  statusMap: { [groupChatId: number]: string | null } = {};
  emojis: any[] = [];   
  showEmojis = false;   
 currentUser: any = null;
  selectedGroup: Group | null = null;
  searchQuery: string = '';
  newMessage: string = '';
private stompSubscription: any;
showJoinGroupModal = false;
 showJoinGroup = false;
 showGroupMembers = false;
private onlineUsersSubject = new BehaviorSubject<{ [groupId: number]: number[] }>({});
public onlineUsers$ = this.onlineUsersSubject.asObservable();
private onlineUsersMap: { [groupId: number]: number[] } = {};
selectedGroupId!: number;
  constructor(private groupService: GroupService, private messageGroupService: MessageGroupService,
    private joinGroupService: JoinGroupService ,private authService: AuthService ,private userService: KeycloakUserService,
    private memberGroupService: MemberGroupService
  ) { }

ngOnInit(): void {

   this.currentUser = this.authService.getUserInfo();
    this.currentUserId = this.currentUser?.id;
  this.groupService.getAllGroups().subscribe((data: any[]) => {
    this.groups = data.map(g => ({
      id: g.id,
      name: g.name,
      avatar: g.image || '👤',
      lastMessage: g.lastMessage || 'Aucun message',
      time: g.time || '',
      unread: g.unread || 0,
      online: g.online || 0,
      ownerId: g.ownerId,
      isMember: false
    }));

   this.checkIsMember(this.groups,this.currentUserId);

   
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
      avatar: data.image || '📁',
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

  userCache: { [id: string]: any } = {};





  
async selectGroup(group: Group): Promise<void>  {


 const canAccess = await this.checkIsMember2(group.id, this.currentUserId);

  if (!canAccess && group.ownerId !== this.currentUserId) {
    Swal.fire({
      icon: 'warning',
      title: 'Access Denied',
      text: 'You cannot access this group. It is private. You must join the group to participate'
    });
    return;
  }

 
  this.selectedGroup = group;
  group.unread = 0;

  // 1️⃣ Déconnecter l'ancien groupe si nécessaire
  this.messageGroupService.disconnect();
  this.stompSubscription?.unsubscribe();

  // 2️⃣ Charger l'historique depuis la DB
  this.messageGroupService.loadHistory(group.id);

  // 3️⃣ Connecter au WebSocket du groupe
  this.messageGroupService.connect(group.id);

  // 4️⃣ S'abonner aux messages reçus en temps réel
  this.stompSubscription = this.messageGroupService.messages$.subscribe(msgs => {
    this.messages = msgs.map(msg => ({
      id: msg.id,                  // correspond à MessageGroup.id
      sender: msg.sender,
      content: msg.content,
      groupChatId: msg.groupChatId, // correspond à MessageGroup.groupChatId
      time: msg.time || new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),
      deleted: msg.deleted || false,
      showDelete: false,
       senderInfo: this.userCache[msg.sender] || null
    }));

 const uniqueSenders = [...new Set(msgs.map(m => m.sender))];
  uniqueSenders.forEach(senderId => {
    if (!this.userCache[senderId]) {
      this.userService.getUserById(senderId.toString()).subscribe({
        next: (user) => {
          this.userCache[senderId] = user;
          this.messages = this.messages.map(m =>
            m.sender === senderId ? { ...m, senderInfo: user } : m
          );
        },
        error: (err) => console.error('Erreur user:', err)
      });
    }
  });

  setTimeout(() => this.scrollToBottom(), 100);
});

  // 5️⃣ Charger le statut de l'utilisateur pour ce groupe
  this.getStatusForGroup(group.id);
  
}


sendMessage(): void {
  if (!this.newMessage.trim() || !this.selectedGroup) return;

  const messageObj = {
    sender: this.currentUserId, // number
    content: this.newMessage,
    groupChatId: this.selectedGroup.id,
    time: new Date().toISOString(), // toujours défini
    deleted: false
  };

  // Sauvegarde dans la DB et envoi WebSocket après
  this.messageGroupService.addMessage(messageObj as any).subscribe({
    next: (savedMessage) => {
      this.messageGroupService.sendMessage(savedMessage);
      this.messages = [...this.messages, savedMessage];
    },
    error: (err) => console.error('Erreur en envoyant le message :', err)
  });

  this.newMessage = '';
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
    alert('Appel vidéo en cours... 📹');
  }

  startAudioCall(): void {
    alert('Appel audio en cours... 📞');
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

openJoinRequests(groupId: number) {
  this.openMenuId = null; // ferme le dropdown
  this.showJoinGroup = true;
   this.selectedGroupId = groupId;
}




openGroupMembers(groupId: number) {
  this.selectedGroupId = groupId;
  this.showGroupMembers = true;
  this.openMenuId = null;
}
closeGroupMembers() {
  this.showGroupMembers = false;
}


isMemberUser: boolean = false;

 async checkIsMember(groups: any[], userId: string) {
  groups.forEach(group => {
    this.memberGroupService.isMember(group.id, userId).subscribe({
      next: (exists: boolean) => {
        // Ajouter un champ isMember à chaque groupe
        group.isMember = exists;
      },
      error: (err) => {
        console.error(`Erreur lors de la vérification du membre pour le groupe ${group.id}`, err);
        group.isMember = false; // par défaut
      }
    });
  });
}

async checkIsMember2(groupId: number, userId: string): Promise<boolean> {
  try {
    // Convertit l'Observable en Promise
    const exists = await this.memberGroupService.isMember(groupId, userId).toPromise();
    return exists ?? false; // ← si undefined, retourne false
  } catch (err) {
    console.error(`Erreur vérification membre groupe ${groupId}`, err);
    return false;
  }
}



toggleLangMenu(message: any) {
  message.showLangMenu = !message.showLangMenu;
}

// ✅ Traduire le message
translateMessage(message: any, language: string) {
  message.showLangMenu = false;
  message.isTranslating = true;
  message.translatedText = '';

  this.messageGroupService.translateMessage(message.content, language).subscribe({
    next: (result) => {
      message.translatedText = result;
      message.isTranslating = false;
    },
    error: (err) => {
      console.error('Erreur traduction:', err);
      message.isTranslating = false;
    }
  });
}

robotResponse: string = '';
isRobotTyping: boolean = false;

askRobot(message: any) {
  if (!message.content) return;

  message.isRobotTyping = true;
  message.robotResponse = '';

  this.messageGroupService.askQuestion(message.content).subscribe({
    next: (result) => {
      message.robotResponse = result;
      message.isRobotTyping = false;
    },
    error: (err) => {
      console.error('Erreur robot:', err);
      message.robotResponse = "Désolé, je n'ai pas pu répondre.";
      message.isRobotTyping = false;
    }
  });
}


}