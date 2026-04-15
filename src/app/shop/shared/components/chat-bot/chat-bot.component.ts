import { Component, inject, signal, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatBotService } from '../../../services/chat-bot.service';

interface ChatMessage {
  user: boolean;
  text: string;
}

@Component({
  selector: 'app-chat-bot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-bot.component.html',
  styleUrls: ['./chat-bot.component.scss']
})
export class ChatBotComponent implements AfterViewChecked {
  private readonly chatService = inject(ChatBotService);
  
  isOpen = signal(false);
  isLoading = signal(false);
  messages = signal<ChatMessage[]>([{
    user: false,
    text: 'Hello! I am your Jungle Assistant. How can I help you find the right product today?'
  }]);
  
  userInput = signal('');

  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;

  toggleChat() {
    this.isOpen.update(v => !v);
  }

  sendMessage() {
    const text = this.userInput().trim();
    if (!text) return;

    // Add user message
    this.messages.update(msgs => [...msgs, { user: true, text }]);
    this.userInput.set('');
    this.isLoading.set(true);

    this.chatService.sendMessage(text).subscribe({
      next: (response) => {
        this.messages.update(msgs => [...msgs, { user: false, text: response }]);
        this.isLoading.set(false);
      },
      error: () => {
        this.messages.update(msgs => [...msgs, { user: false, text: 'Sorry, I am having trouble connecting to the shop right now.' }]);
        this.isLoading.set(false);
      }
    });
  }

  ngAfterViewChecked() {
    if (this.isOpen()) {
      this.scrollToBottom();
    }
  }

  private scrollToBottom(): void {
    if (this.myScrollContainer) {
      try {
        this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
      } catch(err) { }
    }
  }
}
