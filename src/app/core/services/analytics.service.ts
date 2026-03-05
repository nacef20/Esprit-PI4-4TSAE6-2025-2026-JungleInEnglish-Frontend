import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CompletionRateHistory {
    date: string;
    rate: number;
}

export interface StudentEngagement {
    studentId: number;
    username: string;
    overdueCount: number;
    engagementScore: number;
}

export interface AnalyticsStudentDTO {
    personalTotal: number;
    personalCompleted: number;
    personalPending: number;
    personalCompletionRate: number;
    levelTotal: number;
    levelCompleted: number;
    levelOverdue: number;
    averageCompletionTimeHours: number;
    personalStatusBreakdown: Record<string, number>;
    personalPriorityBreakdown: Record<string, number>;
    completionHistory: Record<string, number>;
}

export interface AnalyticsTeacherDTO {
    todosPerLevel: Record<string, number>;
    completionRatePerLevel: Record<string, number>;
    studentEngagement: {
        username: string;
        levelCompletionRate: number;
        personalTodosCount: number;
        overdueCount: number;
        engagementScore: number;
    }[];
    globalCompletionTrend: Record<string, number>;
}

@Injectable({
    providedIn: 'root'
})
export class AnalyticsService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.todoApiUrl}/api/analytics`;

    getStudentStats(): Observable<AnalyticsStudentDTO> {
        return this.http.get<AnalyticsStudentDTO>(`${this.apiUrl}/student`);
    }

    getTeacherStats(): Observable<AnalyticsTeacherDTO> {
        return this.http.get<AnalyticsTeacherDTO>(`${this.apiUrl}/teacher`);
    }
}
