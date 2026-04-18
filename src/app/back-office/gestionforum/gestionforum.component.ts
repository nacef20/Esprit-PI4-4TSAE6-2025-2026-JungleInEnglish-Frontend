import { Component, OnInit, HostListener } from '@angular/core';
import { PostService, PostDTO } from '../../services/post.service';
import { AuthService } from '../../auth/auth.service';
import { CommentService, CommentDTO } from '../../services/comment.service';
import { ReactionService, ReactionDTO } from '../../services/reaction.service';
import { KeycloakUserService} from '../../services/keycloak-user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-gestionforum',
  templateUrl: './gestionforum.component.html',
  styleUrl: './gestionforum.component.scss'
})
export class GestionforumComponent implements OnInit {

  posts: PostDTO[] = [];
  showModal = false;
  loading = false;
  currentUserInitial = 'A';
  currentUserId = '';
  currentUser: any = null;
  selectedMediaType: 'photo' | 'video' | null = null;
  postToEdit: PostDTO | null = null;

  openMenuId: number | null = null;
  openCommentsId: number | null = null;
  openReactionMenuId: number | null = null;

  tooltipPostId: number | null = null;
  tooltipType: string | null = null;
  private tooltipTimer: any = null;

  aiLoadingComment: { [postId: number]: boolean } = {};
  correctedComments: { [postId: number]: string | null } = {};

  aiLoadingSummary: { [postId: number]: boolean } = {};
  postSummaries: { [postId: number]: string | null } = {};

  assistantOpen = false;
  assistantQuestion = '';
  assistantAnswer = '';
  assistantLoading = false;
  assistantHistory: { q: string; a: string }[] = [];

  newComments: { [key: number]: string | undefined } = {};
  reactions: { [postId: number]: ReactionDTO[] } = {};
  top3Posts: any[] = [];
  trendingPosts: PostDTO[] = [];
  isTrendingActive = false;

  // ── MAP NOMS UTILISATEURS ──
  userNames: { [userId: string]: string } = {};

  private reactionEmojis: Record<string, string> = {
    LIKE: '👍', LOVE: '❤️', HAHA: '😂', SAD: '😢', ANGRY: '😠'
  };

  private reactionLabels: Record<string, string> = {
    LIKE: "J'aime", LOVE: 'Adore', HAHA: 'Haha', SAD: 'Triste', ANGRY: 'Grrr'
  };

  constructor(
    private postService: PostService,
    private authService: AuthService,
    private commentService: CommentService,
    private reactionService: ReactionService,
    private userService: KeycloakUserService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getUserInfo();
    this.currentUser = user;
    this.currentUserId = user?.id || '';

    // Charger le vrai nom + initiale de l'utilisateur connecté depuis Keycloak
    if (this.currentUserId) {
      this.userService.getUserById(this.currentUserId).subscribe({
        next: (u) => {
          const fullName = `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim();
          this.currentUserInitial = fullName?.[0]?.toUpperCase() ?? 'A';
          this.userNames[this.currentUserId] = fullName || u.username || this.currentUserId;
        },
        error: () => {
          this.currentUserInitial = user?.firstName?.[0]?.toUpperCase() ?? 'A';
        }
      });
    } else {
      this.currentUserInitial = user?.firstName?.[0]?.toUpperCase() ?? 'A';
    }

    this.commentService.connect();
    this.reactionService.connect();

    setTimeout(() => this.loadPosts(), 500);

    this.commentService.subscribeToDelete((commentId: number) => {
      this.posts.forEach(post => {
        if (post.comments) {
          post.comments = post.comments.filter(c => c.id !== commentId);
        }
      });
    });

    this.loadTop3();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.reaction-wrapper')) this.openReactionMenuId = null;
    if (!target.closest('.menu-wrapper')) this.openMenuId = null;
  }

  // ── NOMS UTILISATEURS ──
  loadUserName(userId: string): void {
    if (!userId || this.userNames[userId]) return;
    this.userService.getUserById(userId).subscribe({
      next: (user) => {
        const fullName = `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim();
        this.userNames[userId] = fullName || user.username || userId;
      },
      error: () => {
        this.userNames[userId] = userId;
      }
    });
  }

  getDisplayName(authorid?: string): string {
    if (!authorid) return 'Utilisateur';
    return this.userNames[authorid] || authorid;
  }

  // ── CHARGEMENT DES POSTS ──
  loadPosts(): void {
    this.loading = true;
    this.postService.getAllPosts().subscribe({
      next: (data) => {
        this.posts = data.map(post => ({ ...post, comments: post.comments || [] }));
        this.loading = false;

        this.posts.forEach(post => {
          // Charger le nom de l'auteur du post
          if (post.authorid) this.loadUserName(post.authorid);

          // Charger les noms des auteurs des commentaires
          post.comments?.forEach(c => {
            if (c.authorid) this.loadUserName(c.authorid);
          });

          // Charger les réactions existantes via HTTP
          this.reactionService.getReactions(post.idpost!).subscribe({
            next: (reactions) => { this.reactions[post.idpost!] = reactions; },
            error: ()          => { this.reactions[post.idpost!] = []; }
          });

          // Souscrire aux événements WS temps réel
          this.listenToReactions(post.idpost!);
        });
      },
      error: () => { this.loading = false; }
    });
  }

  // ── MODAL ──
  openModal(type?: 'photo' | 'video'): void {
    this.selectedMediaType = type ?? null;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedMediaType = null;
    this.postToEdit = null;
  }

  onPostCreated(): void {
    this.closeModal();
    this.loadPosts();
  }

  // ── POSTS ──
  deletePost(id: number): void {
    if (confirm('Supprimer ce post ?')) {
      this.postService.deletePost(id).subscribe(() => this.loadPosts());
    }
  }

  editPost(post: PostDTO): void {
    this.postToEdit = post;
    this.showModal = true;
    this.closeMenu();
  }

  isCurrentUser(authorid?: string): boolean {
    return !!authorid && authorid === this.currentUserId;
  }

  trackById(_: number, post: PostDTO): number {
    return post.idpost!;
  }

  isImage(content?: string): boolean {
    if (!content) return false;
    return /\.(jpg|jpeg|png|gif|webp|svg)(\?.*)?$/i.test(content)
      || content.startsWith('data:image/')
      || (content.includes('/uploads/images/') && !this.isVideo(content));
  }

  isVideo(content?: string): boolean {
    if (!content) return false;
    return /\.(mp4|webm|ogg|mov)(\?.*)?$/i.test(content)
      || content.startsWith('data:video/');
  }

  isText(content?: string): boolean {
    if (!content) return false;
    return !this.isImage(content) && !this.isVideo(content);
  }

  getInitials(authorid?: string): string {
    if (!authorid) return '?';
    return authorid.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase();
  }

  // ── MENU ──
  toggleMenu(id: number): void {
    this.openMenuId = this.openMenuId === id ? null : id;
  }

  closeMenu(): void {
    this.openMenuId = null;
  }

  // ── COMMENTAIRES ──
  toggleComments(postId: number): void {
    this.openCommentsId = this.openCommentsId === postId ? null : postId;
    if (this.openCommentsId === postId) this.listenToComments(postId);
  }

  addComment(postId: number): void {
    const content = this.newComments[postId]?.trim();
    if (!content) return;

    const comment: CommentDTO = { content, authorid: this.currentUser?.id || 'unknown' };

    this.commentService.addComment(postId, comment).subscribe({
      next: () => {
        this.newComments[postId] = '';
        this.correctedComments[postId] = null;
      },
      error: (err) => {
        const message = err?.error?.error || 'Une erreur est survenue.';

        let title = 'Commentaire refusé';
        let icon: 'warning' | 'error' | 'info' = 'warning';

        if (message.includes('spam')) {
          title = '🚫 Spam détecté';
          icon = 'error';
        } else if (message.includes('interdit')) {
          title = '🔗 Lien interdit';
          icon = 'error';
        } else if (message.includes('vite')) {
          title = '⏱️ Trop rapide !';
          icon = 'warning';
        } else if (message.includes('dupliqué')) {
          title = '📋 Doublon détecté';
          icon = 'info';
        }

        Swal.fire({
          title,
          text: message,
          icon,
          confirmButtonText: 'OK',
          confirmButtonColor: '#0ea37a',
          timer: icon === 'warning' ? 3000 : undefined,
          timerProgressBar: icon === 'warning',
        });
      }
    });
  }

  deleteComment(postId: number, commentId: number): void {
    const post = this.posts.find(p => p.idpost === postId);
    if (post?.comments) {
      post.comments = post.comments.filter(c => c.id !== commentId);
    }
    this.commentService.deleteComment(commentId).subscribe({
      error: (err: any) => console.error('Erreur suppression commentaire:', err)
    });
  }

  listenToComments(postId: number): void {
    this.commentService.subscribeToComments(postId, (comment) => {
      const post = this.posts.find(p => p.idpost === postId);
      if (post) {
        if (!post.comments) post.comments = [];
        const exists = post.comments.some(c => c.id === comment.id);
        if (!exists) {
          post.comments.push(comment);
          // Charger le nom du nouvel auteur si inconnu
          if (comment.authorid) this.loadUserName(comment.authorid);
        }
      }
    });
  }

  // ── RÉACTIONS ──
  toggleReactionMenu(postId: number): void {
    if (this.getUserReaction(postId)) {
      this.deleteReaction(postId);
      return;
    }
    this.openReactionMenuId = this.openReactionMenuId === postId ? null : postId;
  }

  react(postId: number, type: 'LIKE' | 'LOVE' | 'HAHA' | 'ANGRY' | 'SAD'): void {
    const existing = this.getUserReaction(postId);
    if (existing === type) {
      this.deleteReaction(postId);
    } else {
      if (existing) this.deleteReaction(postId);
      this.addReaction(postId, type);
    }
    this.openReactionMenuId = null;
  }

  addReaction(postId: number, type: 'LIKE' | 'LOVE' | 'HAHA' | 'ANGRY' | 'SAD'): void {
    const userId = this.currentUser?.id;
    if (!userId) return;

    if (!this.reactions[postId]) this.reactions[postId] = [];
    const idx = this.reactions[postId].findIndex(r => r.userId === userId);
    if (idx > -1) {
      this.reactions[postId][idx] = { userId, type, postId };
    } else {
      this.reactions[postId].push({ userId, type, postId });
    }

    this.reactionService.addReaction(postId, userId, type).subscribe({
      error: (err) => {
        console.error('Erreur ajout réaction:', err);
        if (this.reactions[postId]) {
          this.reactions[postId] = this.reactions[postId].filter(r => r.userId !== userId);
        }
      }
    });
  }

  deleteReaction(postId: number): void {
    const userId = this.currentUser?.id;
    if (!userId) return;

    const previous = this.reactions[postId]?.find(r => r.userId === userId);

    if (this.reactions[postId]) {
      this.reactions[postId] = this.reactions[postId].filter(r => r.userId !== userId);
    }

    this.reactionService.deleteReaction(postId, userId).subscribe({
      error: (err) => {
        console.error('Erreur suppression réaction:', err);
        if (previous) {
          if (!this.reactions[postId]) this.reactions[postId] = [];
          this.reactions[postId].push(previous);
        }
      }
    });
  }

  listenToReactions(postId: number): void {
    this.reactionService.subscribeToReactions(postId, (reaction: ReactionDTO) => {
      if (!this.reactions[postId]) this.reactions[postId] = [];
      const index = this.reactions[postId].findIndex(r => r.userId === reaction.userId);
      if (index > -1) {
        this.reactions[postId][index] = reaction;
      } else {
        this.reactions[postId].push(reaction);
        // Charger le nom du nouvel utilisateur si inconnu
        if (reaction.userId) this.loadUserName(reaction.userId);
      }
    });

    this.reactionService.subscribeToDeleteReactions(postId, (userId: string) => {
      if (this.reactions[postId]) {
        this.reactions[postId] = this.reactions[postId].filter(r => r.userId !== userId);
      }
    });
  }

  // ── HELPERS RÉACTIONS ──
  getUserReaction(postId: number): string | null {
    return this.reactions[postId]?.find(r => r.userId === this.currentUserId)?.type ?? null;
  }

  getUserReactionEmoji(postId: number): string {
    return this.reactionEmojis[this.getUserReaction(postId) ?? ''] ?? '👍';
  }

  getUserReactionLabel(postId: number): string {
    return this.reactionLabels[this.getUserReaction(postId) ?? ''] ?? "J'aime";
  }

  getTotalReactions(postId: number): number {
    return this.reactions[postId]?.length ?? 0;
  }

  getReactionCounts(postId: number): Record<string, number> {
    const counts: Record<string, number> = {};
    for (const r of this.reactions[postId] ?? []) {
      counts[r.type] = (counts[r.type] ?? 0) + 1;
    }
    return counts;
  }

  // ── TOOLTIPS RÉACTIONS ──
  showTooltip(postId: number, type: string): void {
    this.tooltipTimer = setTimeout(() => {
      this.tooltipPostId = postId;
      this.tooltipType   = type;
    }, 120);
  }

  hideTooltip(): void {
    clearTimeout(this.tooltipTimer);
    this.tooltipPostId = null;
    this.tooltipType   = null;
  }

  getTooltipUsers(postId: number, type: string): string[] {
    const all = (this.reactions[postId] ?? [])
      .filter(r => r.type === type)
      .map(r => r.userId === this.currentUserId ? 'Vous' : this.getDisplayName(r.userId));

    const MAX = 5;
    if (all.length <= MAX) return all;

    const visible = all.slice(0, MAX);
    visible.push(`et ${all.length - MAX} autre(s)`);
    return visible;
  }

  // ── 1. CORRIGER UN COMMENTAIRE ──
  correctComment(postId: number): void {
    const text = this.newComments[postId]?.trim();
    if (!text) return;
    this.aiLoadingComment[postId] = true;
    this.postService.correct(text).subscribe({
      next: (result) => {
        this.correctedComments[postId] = result;
        this.aiLoadingComment[postId] = false;
      },
      error: () => { this.aiLoadingComment[postId] = false; }
    });
  }

  applyCorrectedComment(postId: number): void {
    if (this.correctedComments[postId]) {
      this.newComments[postId] = this.correctedComments[postId]!;
      this.correctedComments[postId] = null;
    }
  }

  // ── 2. RÉSUMER UN POST + SES COMMENTAIRES ──
  summarizePost(post: PostDTO): void {
    const comments = (post.comments || []).map(c => `- ${this.getDisplayName(c.authorid)}: ${c.content}`).join('\n');
    const fullText = `Post: ${post.title || ''}\n${post.content || ''}\n\nCommentaires:\n${comments || 'Aucun'}`;
    this.aiLoadingSummary[post.idpost!] = true;
    this.postService.summarize(fullText).subscribe({
      next: (result) => {
        this.postSummaries[post.idpost!] = result;
        this.aiLoadingSummary[post.idpost!] = false;
      },
      error: () => { this.aiLoadingSummary[post.idpost!] = false; }
    });
  }

  closeSummary(postId: number): void {
    this.postSummaries[postId] = null;
  }

  // ── 3. ASSISTANT ANGLAIS ──
  toggleAssistant(): void {
    this.assistantOpen = !this.assistantOpen;
  }

  askAssistant(): void {
    const q = this.assistantQuestion.trim();
    if (!q) return;
    this.assistantLoading = true;
    this.assistantQuestion = '';

    const prompt = `You are an English learning assistant for students. 
Answer in simple English and always add a French translation. 
Student question: ${q}`;

    this.postService.assist(prompt).subscribe({
      next: (answer) => {
        this.assistantHistory.push({ q, a: answer });
        this.assistantLoading = false;
      },
      error: () => {
        this.assistantHistory.push({ q, a: 'Error. Please try again.' });
        this.assistantLoading = false;
      }
    });
  }

  // ── TRENDING ──
  toggleTrending(): void {
    this.isTrendingActive = !this.isTrendingActive;

    if (this.isTrendingActive) {
      this.postService.getTrendingPosts().subscribe({
        next: (data) => {
          this.trendingPosts = data;
          // Charger les noms des auteurs des posts trending
          data.forEach((post: PostDTO) => {
            if (post.authorid) this.loadUserName(post.authorid);
          });
        },
        error: (err) => console.error('Erreur trending:', err)
      });
    }
  }

  get displayedPosts(): PostDTO[] {
    return this.isTrendingActive ? this.trendingPosts : this.posts;
  }

  // ── TOP 3 ──
  loadTop3(): void {
    const now = new Date();
    this.postService.getTop3(now.getMonth() + 1, now.getFullYear()).subscribe({
      next: (data) => {
        this.top3Posts = data;
        // Charger les noms des auteurs du top 3
        data.forEach((p: any) => {
          if (p.authorid) this.loadUserName(p.authorid);
        });
      },
      error: (err) => console.error('Erreur top3:', err)
    });
  }

  getTopRank(authorid: string): number | null {
    const index = this.top3Posts.findIndex(p => p.authorid === authorid);
    return index !== -1 ? index + 1 : null;
  }

  getTopIcon(rank: number | null): string {
    const icons: Record<number, string> = { 1: '🥇', 2: '🥈', 3: '🥉' };
    return rank ? icons[rank] : '';
  }

  get currentUserRank(): number | null {
    return this.getTopRank(this.currentUserId);
  }

  get currentUserScore(): number {
    const found = this.top3Posts.find(p => p.authorid === this.currentUserId);
    return found?.score ?? 0;
  }
}