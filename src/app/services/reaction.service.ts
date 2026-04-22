import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface ReactionDTO {
  idReaction?: number;
  userId: string;
  type: 'LIKE' | 'LOVE' | 'HAHA' | 'ANGRY' | 'SAD';
  postId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReactionService {
  private api = 'http://localhost:8222/reactions';
  private stompClient!: Client;
  private connected = false;
  private pendingCallbacks: (() => void)[] = [];

  constructor(private http: HttpClient) {}

  /**
   * Connexion WebSocket.
   * @param onConnected — callback appelé dès que la connexion est établie.
   *                      Utilisez-le pour lancer loadPosts() APRÈS que le WS est prêt.
   */
  connect(onConnected?: () => void): void {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:9090/ws'),
      reconnectDelay: 5000
    });

    this.stompClient.onConnect = () => {
      console.log('✅ WS Réactions connecté');
      this.connected = true;

      // Exécuter le callback principal (ex: loadPosts)
      if (onConnected) onConnected();

      // Exécuter les callbacks en attente (ex: après reload)
      this.pendingCallbacks.forEach(cb => cb());
      this.pendingCallbacks = [];
    };

    this.stompClient.onDisconnect = () => {
      console.warn('⚠️ WS Réactions déconnecté');
      this.connected = false;
    };

    this.stompClient.activate();
  }

  /**
   * Exécute le callback immédiatement si le WS est connecté,
   * sinon le met en file d'attente jusqu'à la prochaine connexion.
   * Utilisez-le dans onPostCreated() et deletePost() pour éviter
   * de souscrire avant que le WS soit prêt.
   */
  waitForConnection(callback: () => void): void {
    if (this.connected) {
      callback();
    } else {
      this.pendingCallbacks.push(callback);
    }
  }

  subscribeToReactions(postId: number, callback: (reaction: ReactionDTO) => void): void {
    this.stompClient.subscribe(`/topic/reactions/${postId}`, (msg) => {
      callback(JSON.parse(msg.body));
    });
  }

  subscribeToDeleteReactions(postId: number, callback: (userId: string) => void): void {
    this.stompClient.subscribe(`/topic/reactions/delete/${postId}`, (msg) => {
      callback(JSON.parse(msg.body));
    });
  }

  addReaction(postId: number, userId: string, type: string): Observable<ReactionDTO> {
    return this.http.post<ReactionDTO>(
      `${this.api}/post/${postId}?userId=${userId}&type=${type}`, {}
    );
  }

  deleteReaction(postId: number, userId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/post/${postId}?userId=${userId}`);
  }

  getReactions(postId: number): Observable<ReactionDTO[]> {
    return this.http.get<ReactionDTO[]>(`${this.api}/post/${postId}`);
  }
}