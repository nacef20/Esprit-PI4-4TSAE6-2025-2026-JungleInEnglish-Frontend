import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss'
})
export class FooterComponent {

  currentYear = new Date().getFullYear();

  productLinks = ['Download', 'Pricing', 'Locations', 'Server', 'Countries', 'Blog'];
  engageLinks = ['LaslesVPN ?', 'FAQ', 'Tutorials', 'About Us', 'Privacy Policy', 'Terms of Service'];
  earnLinks = ['Affiliate', 'Become Partner'];
}
