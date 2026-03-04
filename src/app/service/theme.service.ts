import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {
    private themeSubject = new BehaviorSubject<string>('dark');
    theme$ = this.themeSubject.asObservable();

    constructor(@Inject(PLATFORM_ID) private platformId: any) {
        if (isPlatformBrowser(this.platformId)) {
            const savedTheme = localStorage.getItem('theme') || 'dark';
            this.setTheme(savedTheme);
        }
    }

    setTheme(theme: string) {
        this.themeSubject.next(theme);
        if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('theme', theme);
            document.body.setAttribute('data-theme', theme);
        }
    }

    toggleTheme() {
        const newTheme = this.themeSubject.value === 'dark' ? 'light' : 'dark';
        this.setTheme(newTheme);
    }

    get currentTheme(): string {
        return this.themeSubject.value;
    }
}
