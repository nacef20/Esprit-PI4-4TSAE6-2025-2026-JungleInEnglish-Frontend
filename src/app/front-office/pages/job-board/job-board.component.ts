import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../auth/auth.service';
import { CvService } from '../../../services/cv.service';

interface JobOffer {
  id?: number;
  title: string;
  description: string;
  location?: string;
  employmentType?: string;
  experienceLevel?: string;
  salary?: number;
  currency?: string;
  postingDate?: string;
  closingDate?: string;
  active?: boolean;
  requiredSkills?: string[];
}

interface AppliedStatus {
  applied: boolean;
  score: number | null; // null = applied but no score yet; number = score available
}

@Component({
  selector: 'app-job-board',
  templateUrl: './job-board.component.html',
  styleUrls: ['./job-board.component.scss']
})
export class JobBoardComponent implements OnInit {
  jobOffers: JobOffer[] = [];
  filteredOffers: JobOffer[] = [];
  searchQuery: string = '';
  locationQuery: string = '';

  loading = true;
  applyingForJobId: number | null = null;
  lastMatchScore: number | null = null;
  showScoreAnimation = false;

  appliedJobs: { [jobId: number]: AppliedStatus } = {};
  error: string | null = null;
  success: string | null = null;

  expandedJobId: number | null = null;

  // ── Active Filters ─────────────────────────────────────────────
  selectedTypes: Set<string>   = new Set();  // e.g. 'FULL_TIME', 'REMOTE'
  selectedLevels: Set<string>  = new Set();  // e.g. 'JUNIOR', 'SENIOR'
  selectedScores: Set<string>  = new Set();  // '70+' | '40+'

  constructor(
    private http: HttpClient,
    public authService: AuthService,
    private cvService: CvService
  ) {}

  ngOnInit() {
    this.fetchJobs();
    this.loadApplications();
  }

  fetchJobs() {
    this.loading = true;
    this.http.get<JobOffer[]>(`${environment.apiUrl}/api/jobs/all`).subscribe({
      next: (offers) => {
        this.jobOffers = offers;
        this.filterJobs();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load job offers.';
        this.loading = false;
      }
    });
  }

  loadApplications() {
    const user = this.authService.getUserInfo();
    if (user && user.id) {
      this.cvService.getLatestMatchesByCandidate(user.id).subscribe({
        next: (matches) => {
          matches.forEach(m => {
            if (m.jobOffer && m.jobOffer.id) {
              // Use null-coalescing to distinguish "score of 0" from "no score"
              const rawScore = m.matchScore;
              const score: number | null = (rawScore !== null && rawScore !== undefined)
                ? Math.round(rawScore)
                : null;
              this.appliedJobs[m.jobOffer.id] = { applied: true, score };
            }
          });
        },
        error: () => {
          // New candidates might not have applications yet — silently ignore
        }
      });
    }
  }

  filterJobs() {
    this.filteredOffers = this.jobOffers.filter(job => {
      // ── Text Search ──────────────────────────────────────────────
      const matchesSearch = !this.searchQuery ||
        job.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        job.description.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        (job.requiredSkills || []).some(s => s.toLowerCase().includes(this.searchQuery.toLowerCase()));

      // ── Location ─────────────────────────────────────────────────
      const matchesLocation = !this.locationQuery ||
        (job.location && job.location.toLowerCase().includes(this.locationQuery.toLowerCase()));

      // ── Employment Type filter ────────────────────────────────────
      // Normalize backend values like FULL_TIME → compare loosely
      const matchesType = this.selectedTypes.size === 0 ||
        [...this.selectedTypes].some(t =>
          (job.employmentType || '').toUpperCase().replace(/[ -]/g, '_') === t
        );

      // ── Experience Level filter ───────────────────────────────────
      const matchesLevel = this.selectedLevels.size === 0 ||
        [...this.selectedLevels].some(l =>
          (job.experienceLevel || '').toUpperCase() === l
        );

      // ── AI Match Score filter ─────────────────────────────────────
      const appStatus = job.id !== undefined ? this.appliedJobs[job.id] : undefined;
      const score = appStatus?.score ?? null;
      const matchesScore = this.selectedScores.size === 0 ||
        [...this.selectedScores].some(s => {
          if (s === '70+') return score !== null && score >= 70;
          if (s === '40+') return score !== null && score >= 40;
          return true;
        });

      return matchesSearch && matchesLocation && matchesType && matchesLevel && matchesScore;
    });
  }

  // ── Filter Toggle Helpers ────────────────────────────────────────
  toggleType(value: string, checked: boolean) {
    checked ? this.selectedTypes.add(value) : this.selectedTypes.delete(value);
    this.filterJobs();
  }

  toggleLevel(value: string, checked: boolean) {
    checked ? this.selectedLevels.add(value) : this.selectedLevels.delete(value);
    this.filterJobs();
  }

  toggleScore(value: string, checked: boolean) {
    checked ? this.selectedScores.add(value) : this.selectedScores.delete(value);
    this.filterJobs();
  }

  clearAllFilters() {
    this.searchQuery = '';
    this.locationQuery = '';
    this.selectedTypes.clear();
    this.selectedLevels.clear();
    this.selectedScores.clear();
    this.filterJobs();
  }

  onFileSelected(event: Event, jobId: number) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    if (!this.authService.isLoggedIn()) {
      this.authService.login();
      return;
    }

    const file = input.files[0];
    const user = this.authService.getUserInfo();

    if (!user || !user.id) {
      this.error = 'User identity not found. Please log in again.';
      return;
    }

    this.applyingForJobId = jobId;
    // Mark as applied immediately, score pending
    this.appliedJobs[jobId] = { applied: true, score: null };

    this.cvService.uploadCV(file, user.id, jobId, user.firstName, user.lastName, user.email).subscribe({
      next: (res: any) => {
        // matchScore may be 0, so use null-coalescing
        const rawScore = res.matchScore;
        const score: number | null = (rawScore !== null && rawScore !== undefined)
          ? Math.round(rawScore)
          : null;
        this.appliedJobs[jobId] = { applied: true, score };
        this.lastMatchScore = score;
        this.showScoreAnimation = true;
        this.applyingForJobId = null;

        setTimeout(() => {
          this.showScoreAnimation = false;
        }, 6000);
      },
      error: (err: any) => {
        // If already applied, just mark the application without clearing it
        if (err.status === 400) {
          this.appliedJobs[jobId] = { applied: true, score: null };
          this.error = 'You have already applied to this job offer.';
        } else {
          delete this.appliedJobs[jobId];
          this.error = err.error || 'Failed to submit CV. Please try again.';
        }
        this.applyingForJobId = null;
        setTimeout(() => this.error = null, 5000);
      }
    });
    input.value = '';
  }

  getScoreColor(score: number): string {
    if (score >= 70) return '#10b981';
    if (score >= 40) return '#f59e0b';
    return '#ef4444';
  }

  getScoreLabel(score: number): string {
    if (score >= 70) return 'Excellent Match';
    if (score >= 40) return 'Good Match';
    return 'Low Match';
  }

  getScoreGlow(score: number): string {
    if (score >= 70) return '0 0 20px rgba(16, 185, 129, 0.5)';
    if (score >= 40) return '0 0 20px rgba(245, 158, 11, 0.5)';
    return '0 0 20px rgba(239, 68, 68, 0.5)';
  }

  toggleDetails(jobId: number) {
    this.expandedJobId = this.expandedJobId === jobId ? null : jobId;
  }
}
