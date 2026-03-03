import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GroupService {

private apiUrl = 'http://localhost:8222'; 

  constructor(private http: HttpClient) { }


  getAllGroups(): Observable<any> {
    return this.http.get(`${this.apiUrl}/chatPrivee/all`);
  }

  getGroupById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/chatPrivee/${id}`);
  }

  createGroup(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/chatPrivee/add`, data);
  }

  updateGroup(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/chatPrivee/update/${id}`, data);
  }

 deleteGroup(id: number) {
  return this.http.delete(`${this.apiUrl}/chatPrivee/delete/${id}`, { responseType: 'text' });
}


uploadImage(file: File): Observable<string> {
  const formData = new FormData();
  formData.append('file', file);

  return this.http.post(`${this.apiUrl}/chatPrivee/upload`, formData, { responseType: 'text' });
}


  
}
