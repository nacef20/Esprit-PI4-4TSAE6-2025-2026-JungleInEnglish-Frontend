import { Injectable } from '@angular/core';
import { getKeycloak } from './keycloak.config';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
constructor(private http: HttpClient) {}
 login(): void {
    getKeycloak().login({
      redirectUri: window.location.origin + '/admin' 
    });
  }

  logout(): void {
    getKeycloak().logout({
      redirectUri: window.location.origin
    });
  }

  isLoggedIn(): boolean {
    return !!getKeycloak().token;
  }

  getRoles(): string[] {
    return getKeycloak().tokenParsed?.realm_access?.roles || [];
  }


  getToken(): string {
  return getKeycloak().token || '';
}

getUserInfo() {
  const tokenParsed = getKeycloak().tokenParsed;
  if (tokenParsed) {
    return {
      id: tokenParsed['sub'], 
      username: tokenParsed['preferred_username'],
      firstName: tokenParsed['given_name'],
      lastName: tokenParsed['family_name'],
      email: tokenParsed['email'],
      roles: tokenParsed['realm_access']?.roles || []
    };
  }
  return null;
}
isTeacher(): boolean {
  return this.getRoles().includes('TEACHER');
}

isStudent(): boolean {
  return this.getRoles().includes('STUDENT');
}
  getMe() {
    return this.http.get<any>('http://localhost:8222/api/users/me');
  }
}
