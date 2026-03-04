import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { NotificationService, Notification } from '../../service/notification.service';
import { AuthService } from '../../service/auth.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
    selector: 'app-notifications',
    standalone: true,
    imports: [CommonModule, RouterModule, NavbarComponent],
    templateUrl: './notifications.component.html',
    styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit {
    notifications: Notification[] = [];
    loading = false;

    constructor(
        private notificationService: NotificationService,
        private authService: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.refresh();
    }

    refresh(): void {
        const user = this.authService.getCurrentUser();
        if (!user?.id) return;

        this.loading = true;
        this.notificationService.getNotifications(user.id).subscribe({
            next: (res) => {
                this.notifications = res.notifications || [];
                this.loading = false;
            },
            error: () => {
                this.loading = false;
            }
        });
    }

    markAsRead(event: Event, n: Notification): void {
        event.stopPropagation();
        const user = this.authService.getCurrentUser();
        if (!user?.id) return;

        this.notificationService.markAsRead(n.id, user.id).subscribe(() => {
            n.read = true;
        });
    }

    onNotificationClick(n: Notification): void {
        if (!n.read) {
            const user = this.authService.getCurrentUser();
            if (user?.id) {
                this.notificationService.markAsRead(n.id, user.id).subscribe(() => {
                    n.read = true;
                });
            }
        }

        if (n.jobOfferId) {
            this.router.navigate(['/admin/job-offers', n.jobOfferId]);
        } else {
            this.router.navigate(['/admin']);
        }
    }
}
