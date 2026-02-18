import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface JoinGroupDTO {
  idJoinGroup?: number;
  
  requestedAt?: string;
  groupChatId: number;
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

  // Récupérer une demande par id
  getById(id: number): Observable<JoinGroupDTO> {
    return this.http.get<JoinGroupDTO>(`${this.apiUrl}/${id}`);
  }

  // Mettre à jour une demande (ex: accepter/refuser)
  update(id: number, dto: JoinGroupDTO): Observable<JoinGroupDTO> {
    return this.http.put<JoinGroupDTO>(`${this.apiUrl}/update/${id}`, dto);
  }

  // Supprimer une demande
  delete(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/delete/${id}`);
  }

  getStatus(userId: number, groupChatId: number): Observable<string> {
  return this.http.get<string>(`${this.apiUrl}/status`, {
    params: { userId, groupChatId }
  });
}

}
