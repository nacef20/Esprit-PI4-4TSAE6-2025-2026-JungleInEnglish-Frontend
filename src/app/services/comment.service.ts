import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import SockJS from 'sockjs-client';

import { Client } from '@stomp/stompjs';

export interface CommentDTO {
  id?: number;
  content: string;
  authorid?: string;
  dateSend?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {

private stompClient!: Client;

 private api = 'http://localhost:8222/comments';


subscribeToComments(postId: number, callback: (comment: CommentDTO) => void) {
  this.stompClient.subscribe(`/topic/comments/${postId}`, (message) => {
    const comment = JSON.parse(message.body);
    callback(comment);
  });
}

subscribeToDelete(callback: (id: number) => void) {
  if (!this.stompClient || !this.stompClient.connected) return;

  this.stompClient.subscribe(`/topic/comments/delete`, (message) => {
    const id = JSON.parse(message.body);
    callback(id);
  });
}

  constructor(private http: HttpClient) {}

  // Ajouter un commentaire
  addComment(postId: number, comment: CommentDTO): Observable<CommentDTO> {
    return this.http.post<CommentDTO>(`${this.api}/post/${postId}`, comment);
  }

  // Récupérer les commentaires d'un post
  getCommentsByPost(postId: number): Observable<CommentDTO[]> {
    return this.http.get<CommentDTO[]>(`${this.api}/post/${postId}`);
  }

  // Supprimer un commentaire
  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${commentId}`);
  }


   connect(onConnected?: () => void) {
  this.stompClient = new Client({
    webSocketFactory: () => new SockJS('http://localhost:9090/ws'),
    reconnectDelay: 5000
  });

  this.stompClient.onConnect = () => {
    console.log('✅ WebSocket connecté');

    if (onConnected) {
      onConnected(); 
    }
  };

  this.stompClient.activate();
}

}
