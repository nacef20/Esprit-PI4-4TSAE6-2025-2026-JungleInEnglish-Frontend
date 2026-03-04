import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Notification {
  id: number;
  candidateFullName: string;
  jobOfferTitle: string;
  matchingScore: number;
  applicationDate: string;
  read: boolean;
  recipientRole: string;
  employerId?: number;
  jobOfferId?: number;
}

export interface NotificationsResponse {
  notifications: Notification[];
  unreadCount: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getNotifications(userId: number): Observable<NotificationsResponse> {
    return this.http.get<NotificationsResponse>(
      `${this.apiUrl}/api/notifications?userId=${userId}`
    );
  }

  markAsRead(notificationId: number, userId: number): Observable<Notification> {
    return this.http.put<Notification>(
      `${this.apiUrl}/api/notifications/${notificationId}/read?userId=${userId}`,
      {}
    );
  }
}
