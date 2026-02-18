import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Même structure que ton DTO côté backend
export interface MessageGroup {
  id?: number;
  sender: number; // ou string si tu stockes le nom
  content: string;
  dateSend?: string; // ou Date
}
@Injectable({
  providedIn: 'root'
})
export class MessageGroupService {

   private apiUrl = 'http://localhost:8222/messageGroup';

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
 

}
