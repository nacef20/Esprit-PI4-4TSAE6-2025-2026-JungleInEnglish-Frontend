import { Component } from '@angular/core';



@Component({
  selector: 'app-back-office',
  templateUrl: './back-office.component.html',
  styleUrl: './back-office.component.scss'
})
export class BackOfficeComponent {
  sidebarCollapsed: boolean = false;
  ngOnInit(): void {
    
    const savedState = localStorage.getItem('sidebarCollapsed');
    if (savedState !== null) {
      this.sidebarCollapsed = JSON.parse(savedState);
    }

   
    window.addEventListener('storage', (event) => {
      if (event.key === 'sidebarCollapsed' && event.newValue !== null) {
        this.sidebarCollapsed = JSON.parse(event.newValue);
      }
    });
  }
}
