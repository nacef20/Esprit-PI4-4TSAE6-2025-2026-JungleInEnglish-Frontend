import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GroupMemberDTO {
  idGroupMember?: number;
  userId: string;
  joinedAt?: string;
  groupChatId?: number;
   firstName: string;
  lastName: string;
}

@Injectable({
  providedIn: 'root'
})
export class MemberGroupService {

  private baseUrl = 'http://localhost:8222/membreGroupChat';

  constructor(private http: HttpClient) {}

  // 🔹 Get members of a specific group
  getMembersByGroup(groupId: number): Observable<GroupMemberDTO[]> {
    return this.http.get<GroupMemberDTO[]>(
      `${this.baseUrl}/group/${groupId}`
    );
  }

  // 🔹 Add member
  addMember(dto: GroupMemberDTO): Observable<GroupMemberDTO> {
    return this.http.post<GroupMemberDTO>(`${this.baseUrl}/add`, dto);
  }

removeMember(groupId: number, userId: string): Observable<void> {
  return this.http.delete<void>(`${this.baseUrl}/${groupId}/members/${userId}`);
}


isMember(groupId: number, userId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/is-member?groupId=${groupId}&userId=${userId}`);
  }



  

}
