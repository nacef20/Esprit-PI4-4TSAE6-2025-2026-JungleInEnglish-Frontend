import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../constants/api.constants';

const URL = `${API_CONFIG.apiUrl}/shop/bot/chat`;

@Injectable({ providedIn: 'root' })
export class ChatBotService {
    private readonly http = inject(HttpClient);

    sendMessage(message: string): Observable<string> {
        return this.http.post(URL, { message }, { responseType: 'text' });
    }
}
