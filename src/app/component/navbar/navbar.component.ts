import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { NotificationService, Notification } from '../../service/notification.service';
import { ThemeService } from '../../service/theme.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  template: `
    <nav class="jungle-nav sticky-top">
      <div class="brand" (click)="goToHome()" style="cursor: pointer;">
        <i class="fas fa-leaf me-2 text-success"></i>
        <span class="jungle-text">Jungle In English</span>
      </div>

      <div class="nav-links" *ngIf="userRole !== 'CANDIDATE'">
        <a class="nav-link" 
           [class.active]="currentPath === '/admin' && !isStatsView" 
           (click)="navigate('/admin')">Job Offers</a>
        <a class="nav-link" 
           [class.active]="isStatsView" 
           (click)="navigate('/admin', { view: 'stats' })">Statistique</a>
        <a class="nav-link" 
           *ngIf="userRole === 'ADMIN'"
           [class.active]="currentPath === '/users'" 
           (click)="navigate('/users')">User Management</a>
        <div class="nav-link position-relative d-inline-flex align-items-center notification-trigger" style="cursor: pointer;">
          <a class="d-flex align-items-center" (click)="toggleNotifications()" title="Notifications">
            <i class="fas fa-bell"></i>
            <span *ngIf="notificationUnreadCount > 0" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">{{ notificationUnreadCount }}</span>
          </a>
          <div *ngIf="showNotificationsDropdown" class="notifications-dropdown shadow-lg" (click)="$event.stopPropagation()">
            <div class="dropdown-header">Notifications</div>
            <div *ngIf="notificationsLoading" class="dropdown-item text-muted small">Loading...</div>
            <div *ngIf="!notificationsLoading && notifications.length === 0" class="dropdown-item text-muted small">No notifications yet</div>
            <div *ngFor="let n of notifications" class="dropdown-item notification-item" [class.unread]="!n.read"
                 (click)="onNotificationClick(n)">
              <div class="fw-semibold small">{{ n.candidateFullName }} applied to {{ n.jobOfferTitle }}</div>
              <div class="text-muted small">Match: {{ n.matchingScore | number:'1.1-1' }}% · {{ n.applicationDate | date:'short' }}</div>
            </div>
            <div class="dropdown-footer text-center p-2 border-top border-secondary border-opacity-10">
              <a class="text-success small fw-bold text-decoration-none" 
                 (click)="navigate('/admin/notifications'); closeNotifications()" 
                 style="cursor: pointer;">See All Notifications</a>
            </div>
          </div>
        </div>
      </div>

      <div class="nav-links" *ngIf="userRole === 'CANDIDATE'">
        <a class="nav-link active">Job Opportunities</a>
      </div>

      <div class="user-area">
        <button class="theme-toggle-btn" (click)="toggleTheme()" [title]="'Switch to ' + (currentTheme === 'dark' ? 'light' : 'dark') + ' mode'">
          <i class="fas" [ngClass]="currentTheme === 'dark' ? 'fa-sun' : 'fa-moon'"></i>
        </button>
        <div class="user-info d-none d-sm-flex">
          <span class="user-name">{{ userName }}</span>
          <span class="user-role badge border border-secondary border-opacity-25">{{ userRole }}</span>
        </div>
        <button class="jungle-btn jungle-btn-danger" (click)="logout()">
          <i class="fas fa-sign-out-alt me-2 d-none d-md-inline"></i> Logout
        </button>
      </div>
    </nav>
  `,
  styles: [`
    .user-area {
      display: flex;
      align-items: center;
      gap: 1.5rem;
    }
    .theme-toggle-btn {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      color: #94a3b8;
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .theme-toggle-btn:hover {
      background: rgba(16, 185, 129, 0.1);
      color: var(--primary-color);
      border-color: var(--primary-color);
    }
    [data-theme='light'] .theme-toggle-btn {
      background: rgba(0, 0, 0, 0.05);
      border-color: rgba(0, 0, 0, 0.1);
      color: #475569;
    }
    .user-info {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      line-height: 1.2;
    }
    .user-name {
      font-weight: 700;
      font-size: 0.9rem;
      color: var(--text-primary);
    }
    .user-role {
      font-size: 0.7rem;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      padding: 0.1rem 0.4rem;
      border-radius: 4px;
    }
    .jungle-text {
      font-weight: 800;
      color: var(--primary-color);
      letter-spacing: -0.02em;
    }
    .notification-trigger {
      position: relative;
    }
    .notifications-dropdown {
      position: absolute;
      top: 100%;
      right: 0;
      margin-top: 0.5rem;
      min-width: 320px;
      max-width: 400px;
      max-height: 400px;
      overflow-y: auto;
      background: var(--surface-color);
      border-radius: 12px;
      border: 1px solid var(--border-color);
      z-index: 1050;
    }
    .dropdown-header {
      padding: 0.75rem 1rem;
      font-weight: 700;
      font-size: 0.9rem;
      border-bottom: 1px solid var(--border-color);
    }
    .dropdown-item.notification-item {
      padding: 0.6rem 1rem;
      cursor: pointer;
      border-bottom: 1px solid var(--border-color);
      transition: background 0.15s;
    }
    .dropdown-item.notification-item:hover {
      background: rgba(255,255,255,0.05);
    }
    .dropdown-item.notification-item.unread {
      background: rgba(34, 197, 94, 0.08);
    }
  `],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class NavbarComponent implements OnInit, OnDestroy {
  userName = '';
  userRole = '';
  currentPath = '';
  notificationUnreadCount = 0;
  showNotificationsDropdown = false;
  notifications: Notification[] = [];
  notificationsLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    private themeService: ThemeService
  ) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.currentPath = event.urlAfterRedirects;
    });
  }

  ngOnInit() {
    const user = this.authService.getCurrentUser();
    this.userName = user.name || 'User';
    this.userRole = user.role || 'CANDIDATE';
    this.currentPath = this.router.url;
    if ((this.userRole === 'ADMIN' || this.userRole === 'EMPLOYER') && user.id) {
      this.loadNotificationCount();
    }
    document.addEventListener('click', this.boundHandleClickOutside);
  }

  private boundHandleClickOutside = (ev: Event) => this.handleClickOutside(ev);

  ngOnDestroy() {
    document.removeEventListener('click', this.boundHandleClickOutside);
  }

  get currentTheme(): string {
    return this.themeService.currentTheme;
  }

  toggleTheme() {
    this.themeService.toggleTheme();
  }

  private handleClickOutside(ev: Event) {
    const target = ev.target as HTMLElement;
    if (!target.closest('.notification-trigger')) {
      this.closeNotifications();
    }
  }

  loadNotificationCount() {
    const user = this.authService.getCurrentUser();
    if (!user?.id) return;
    this.notificationService.getNotifications(user.id).subscribe(res => {
      this.notificationUnreadCount = res.unreadCount;
    });
  }

  toggleNotifications() {
    this.showNotificationsDropdown = !this.showNotificationsDropdown;
    if (this.showNotificationsDropdown) {
      this.fetchNotifications();
    }
  }

  closeNotifications() {
    this.showNotificationsDropdown = false;
  }

  fetchNotifications() {
    const user = this.authService.getCurrentUser();
    if (!user?.id) return;
    this.notificationsLoading = true;
    this.notificationService.getNotifications(user.id).subscribe({
      next: res => {
        this.notifications = res.notifications || [];
        this.notificationUnreadCount = res.unreadCount;
        this.notificationsLoading = false;
      },
      error: () => {
        this.notificationsLoading = false;
      }
    });
  }

  onNotificationClick(n: Notification) {
    if (!n.read) {
      const user = this.authService.getCurrentUser();
      if (user?.id) {
        this.notificationService.markAsRead(n.id, user.id).subscribe(() => {
          n.read = true;
          this.loadNotificationCount();
        });
      }
    }
    if (n.jobOfferId) {
      this.router.navigate(['/admin/job-offers', n.jobOfferId]);
    } else {
      this.router.navigate(['/admin']);
    }
    this.closeNotifications();
  }

  navigate(path: string, queryParams: any = {}) {
    this.router.navigate([path], { queryParams });
  }

  get isStatsView(): boolean {
    return this.router.url.includes('view=stats');
  }

  goToHome() {
    if (this.userRole === 'CANDIDATE') {
      this.router.navigate(['/dashboard']);
    } else {
      this.router.navigate(['/admin']);
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
