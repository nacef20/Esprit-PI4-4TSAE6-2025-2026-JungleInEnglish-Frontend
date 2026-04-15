import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {  BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
// Même structure que ton DTO côté backend
export interface MessageGroup {
  id: number;
  sender: number;
  content: string;
  groupChatId: number;
  time: string;
  showDelete?: boolean;  
  deleted?: boolean;
}
@Injectable({
  providedIn: 'root'
})
export class MessageGroupService {

  private apiUrl = `${environment.apiBaseUrl}/messageGroup`;
  private wsUrl  = 'http://localhost:7070/ws';

    private messagesSubject = new BehaviorSubject<MessageGroup[]>([]);
  public messages$ = this.messagesSubject.asObservable();
  constructor(private http: HttpClient) { }

  // Ajouter un message
  addMessage(message: MessageGroup): Observable<MessageGroup> {
    return this.http.post<MessageGroup>(`${this.apiUrl}/add`, message);
  }

 getMessagesByGroupId(groupId: number): Observable<MessageGroup[]> {
    return this.http.get<MessageGroup[]>(`${this.apiUrl}/group/${groupId}`);
  }


  // Récupérer tous les messages
  getAllMessages(): Observable<MessageGroup[]> {
    return this.http.get<MessageGroup[]>(`${this.apiUrl}/all`);
  }

  // Récupérer un message par id
  getMessageById(id: number): Observable<MessageGroup> {
    return this.http.get<MessageGroup>(`${this.apiUrl}/${id}`);
  }

  // Mettre à jour un message
  updateMessage(id: number, message: MessageGroup): Observable<MessageGroup> {
    return this.http.put<MessageGroup>(`${this.apiUrl}/update/${id}`, message);
  }

  // Supprimer un message
  softDeleteMessage(id: number): Observable<any> {
  return this.http.put(`${this.apiUrl}/delete/${id}`, null, { responseType: 'text' });
}


  loadEmojis(): Observable<any[]> {
    return this.http.get<any[]>(
      'https://emojihub.yurace.pro/api/all/category/smileys-and-people'
    );
  }


stompClient!: Client;

 connect(groupId: number): void {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(this.wsUrl),
      reconnectDelay: 5000,

      onConnect: () => {
        console.log('✅ WebSocket connecté — groupe', groupId);

        this.stompClient.subscribe(`/topic/group/${groupId}`, (frame) => {
          const msg: MessageGroup = JSON.parse(frame.body);

          // Ajoute le message reçu à la liste
          const current = this.messagesSubject.getValue();
          this.messagesSubject.next([...current, msg]);
        });
      },

      onStompError: (frame) => {
        console.error('❌ Erreur STOMP :', frame.headers['message']);
      },

      onDisconnect: () => {
        console.log('🔌 WebSocket déconnecté');
      }
    });

    this.stompClient.activate();
  }

  // ✅ Méthode manquante dans ton service original
  sendMessage(message: MessageGroup): void {
    if (!this.stompClient?.connected) {
      console.warn('⚠️ STOMP non connecté, impossible d\'envoyer');
      return;
    }

    this.stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(message)
    });
  }

  // Charge l'historique depuis la DB et initialise le BehaviorSubject
  loadHistory(groupId: number): void {
    this.getMessagesByGroupId(groupId).subscribe({
      next: (msgs) => this.messagesSubject.next(msgs),
      error: (err) => console.error('Erreur chargement historique :', err)
    });
  }

  disconnect(): void {
    this.stompClient?.deactivate();
    this.messagesSubject.next([]);
  }


  // ✅ Traduire un message
  translateMessage(text: string, language: string): Observable<string> {
    return this.http.post<string>(
      `${this.apiUrl}/translate`,
      { text, language },
      { responseType: 'text' as 'json' }
    );
  }

  // ✅ Poser une question
  askQuestion(question: string): Observable<string> {
    return this.http.post<string>(
      `${this.apiUrl}/ask`,
      { question },
      { responseType: 'text' as 'json' }
    );
  }

}
