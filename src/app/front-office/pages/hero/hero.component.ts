import { Component } from '@angular/core';

@Component({
  selector: 'app-hero',
  templateUrl: './hero.component.html',
  styleUrl: './hero.component.scss'
})
export class HeroComponent {
  listUser = [
    {
      name: "Users",
      number: "390",
      icon: "assets/Icon/heroicons_sm-user.svg",
    },
    {
      name: "Locations",
      number: "20",
      icon: "assets/Icon/gridicons_location.svg",
    },
    {
      name: "Server",
      number: "50",
      icon: "assets/Icon/bx_bxs-server.svg",
    },
  ];
}
