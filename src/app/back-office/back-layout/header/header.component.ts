
import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../auth/auth.service';
@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {

userName: string = 'Jean Dupont';
  userRole: string = 'Administrateur';
  userAvatar: string = 'assets/avatar.jpg'; // Chemin vers votre image

  // UI state
  isUserMenuOpen: boolean = false;
  isNotificationsOpen: boolean = false;
  isDarkMode: boolean = false;
  notificationCount: number = 3;
  searchQuery: string = '';
currentUser: any = null;
  constructor(private router: Router,private authService: AuthService) {}

  ngOnInit(): void {
    // Charger le thème sauvegardé
    this.loadTheme();
    this.currentUser = this.authService.getUserInfo();
    // Vous pouvez charger les données utilisateur depuis un service
    // this.loadUserData();
  }

  // Fermer les menus quand on clique ailleurs
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    
    if (!target.closest('.user-menu')) {
      this.isUserMenuOpen = false;
    }
  }

  // Toggle user menu
  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  // Toggle notifications
  toggleNotifications(): void {
    this.isNotificationsOpen = !this.isNotificationsOpen;
    // Vous pouvez ouvrir un panneau de notifications ici
    console.log('Notifications clicked');
  }

  // Toggle dark mode
  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    
    if (this.isDarkMode) {
      document.body.classList.add('dark-mode');
      localStorage.setItem('theme', 'dark');
    } else {
      document.body.classList.remove('dark-mode');
      localStorage.setItem('theme', 'light');
    }
  }

  // Load theme from localStorage
  loadTheme(): void {
    const savedTheme = localStorage.getItem('theme');
    
    if (savedTheme === 'dark') {
      this.isDarkMode = true;
      document.body.classList.add('dark-mode');
    } else {
      this.isDarkMode = false;
      document.body.classList.remove('dark-mode');
    }
  }

  // Navigation
  navigateTo(route: string): void {
    this.isUserMenuOpen = false;
    this.router.navigate([`/${route}`]);
  }


  

  // Logout
  logout() {
    this.authService.logout();
  }


  // Charger les données utilisateur (à implémenter avec votre service)
  loadUserData(): void {
    // Exemple avec un service
    // this.userService.getCurrentUser().subscribe(user => {
    //   this.userName = user.name;
    //   this.userRole = user.role;
    //   this.userAvatar = user.avatar;
    // });
  }

}
