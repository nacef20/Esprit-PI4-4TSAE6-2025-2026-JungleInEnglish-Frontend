import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface PostDTO {
  idpost?: number;
  title: string;
  content: string;
    authorid?: string;  

  createdAt?: string;
  comments?: any[];
  reactions?: any[];
}
@Injectable({
  providedIn: 'root'
})
export class PostService {

 


  private api = 'http://localhost:8222/posts';
  constructor(private http: HttpClient) {}


  getAllPosts():               Observable<PostDTO[]>  { return this.http.get<PostDTO[]>(`${this.api}/all`); }
   getPostById(id: number):    Observable<PostDTO>    { return this.http.get<PostDTO>(`${this.api}/${id}`); }
   createPost(post: PostDTO):  Observable<PostDTO>    { return this.http.post<PostDTO>(`${this.api}/add`, post); }
   updatePost(id: number, p: PostDTO): Observable<PostDTO> { return this.http.put<PostDTO>(`${this.api}/update/${id}`, p); }
  deletePost(id: number):     Observable<void>       { return this.http.delete<void>(`${this.api}/delete/${id}`); }

uploadFile(file: File): Observable<string> {
  const formData = new FormData();
  formData.append('file', file);
  return this.http.post(`${this.api}/upload`, formData, { responseType: 'text' });
}


 correct(text: string): Observable<string> {
    return this.http.post(`${this.api}/correct`, text, {
      responseType: 'text',
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  // ✅ Résumé
  summarize(text: string): Observable<string> {
    return this.http.post(`${this.api}/summarize`, text, {
      responseType: 'text',
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  // ✅ Assistant
  assist(text: string): Observable<string> {
    return this.http.post(`${this.api}/assist`, text, {
      responseType: 'text',
      headers: { 'Content-Type': 'text/plain' }
    });
  }

getTrendingPosts(): Observable<PostDTO[]> {
    return this.http.get<PostDTO[]>(`${this.api}/trending`);
}

getTop3(month: number, year: number): Observable<any[]> {
  return this.http.get<any[]>(`${this.api}/top3/${month}/${year}`);
}


}

