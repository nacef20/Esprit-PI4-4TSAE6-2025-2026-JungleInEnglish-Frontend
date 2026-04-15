import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageService } from '../../../services/message.service';

@Component({
  selector: 'app-message-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message-toast.component.html',
  styleUrl: './message-toast.component.css',
})
export class MessageToastComponent {
  readonly messageService = inject(MessageService);
}
