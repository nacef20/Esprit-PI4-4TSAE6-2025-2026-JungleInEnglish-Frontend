import { Injectable, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
    private readonly THEME_KEY = 'jungle-theme';
    readonly theme = signal<Theme>(this.getInitialTheme());

    constructor() {
        this.applyTheme(this.theme());
    }

    toggleTheme(): void {
        const newTheme = this.theme() === 'light' ? 'dark' : 'light';
        this.theme.set(newTheme);
        localStorage.setItem(this.THEME_KEY, newTheme);
        this.applyTheme(newTheme);
    }

    private getInitialTheme(): Theme {
        const saved = localStorage.getItem(this.THEME_KEY) as Theme;
        if (saved) return saved;
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    private applyTheme(theme: Theme): void {
        document.documentElement.setAttribute('data-theme', theme);
    }
}
