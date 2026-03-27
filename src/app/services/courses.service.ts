import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Course } from '../models/course.model';

@Injectable({
  providedIn: 'root'  
})
export class CoursesService {
private apiUrl = `${environment.apiUrl}/api/courses`;
  constructor(private http: HttpClient) { }

  createCourse(course: Course): Observable<Course> {
    return this.http.post<Course>(`${this.apiUrl}/addCourse`, course);
  }

  getCourse(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.apiUrl}/getCourse/${id}`);
  }

getAllCourses(search?: string, level?: string): Observable<Course[]> {
  const params: any = {};
  if (search && search.trim()) params.search = search.trim();
  if (level) params.level = level;

  return this.http.get<Course[]>(`${this.apiUrl}/getAllcourses`, { params });
}


  updateCourse(id: number, course: Course): Observable<Course> {
    return this.http.put<Course>(`${this.apiUrl}/updateEntreprise/${id}`, course);
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/deleteCourse/${id}`);
  }
getCoursesByIds(ids: number[]): Observable<Course[]> {
  return this.http.post<Course[]>(`${this.apiUrl}/by-ids`, ids);
}
}