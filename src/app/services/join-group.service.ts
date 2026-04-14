import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface JoinGroupDTO {
  idJoinGroup?: number;
  userId: string;
  requestedAt?: string;
  groupChatId: number;
  status?: 'ATTENTE' | 'ACCEPTE' | 'REFUSE';
  firstName?: string;
  lastName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class JoinGroupService {

 private apiUrl = 'http://localhost:8222/joinGroup';

  constructor(private http: HttpClient) {}

  // Envoyer une demande
  addRequest(dto: JoinGroupDTO): Observable<JoinGroupDTO> {
    return this.http.post<JoinGroupDTO>(`${this.apiUrl}/add`, dto);
  }

  // Récupérer toutes les demandes
  getAll(): Observable<JoinGroupDTO[]> {
    return this.http.get<JoinGroupDTO[]>(`${this.apiUrl}/all`);
  }


  getStatus(userId: number, groupChatId: number): Observable<string> {
  return this.http.get<string>(`${this.apiUrl}/status`, {
    params: { userId, groupChatId }
  });

}


getAllByGroup(groupChatId: number): Observable<JoinGroupDTO[]> {
    return this.http.get<JoinGroupDTO[]>(`${this.apiUrl}/group/${groupChatId}`);
  }


  accepterDemande(id: number): Observable<void> {
  return this.http.put<void>(`${this.apiUrl}/accept/${id}`, {});
}

refuserDemande(id: number): Observable<void> {
  return this.http.put<void>(`${this.apiUrl}/reject/${id}`, {});
}


}
