import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from './navbar/navbar.component';

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
  requiredQualifications?: string[];
  requiredLanguages?: string[];
  requiredCertifications?: string[];
  showDetails?: boolean; // for toggling candidate table
}

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, NavbarComponent]
})
export class AdminDashboardComponent implements OnInit {
  jobOffers: JobOffer[] = [];
  jobForm: Partial<JobOffer> = {
    title: '',
    description: '',
    requiredSkills: [],
    requiredQualifications: [],
    requiredLanguages: [],
    requiredCertifications: []
  };
  requiredSkillsString = '';
  requiredQualificationsString = '';
  requiredLanguagesString = '';
  requiredCertificationsString = '';
  editingJobId: number | null = null;
  error: string | null = null;
  success: string | null = null;
  employerId: number | null = null;
  userRole: string | null = null;
  candidateMatches: { [jobId: number]: any[] } = {};
  showCandidatesModal = false;
  selectedJobForCandidates: JobOffer | null = null;
  selectedCandidate: any = null;
  showCVModal = false;
  showStatistics = false;
  stats: any = {};
  showFilters: boolean = false;
  searchQuery: string = '';
  rankingMode = false;
  showDeleteModal = false;
  showJobModal = false;
  jobToDelete: JobOffer | null = null;
  focusedJob: JobOffer | null = null;
  showShortlisted = false;
  shortlistedMatches: any[] = [];
  minDate: string = '';

  constructor(private router: Router, private http: HttpClient, private route: ActivatedRoute) {
    this.route.queryParams.subscribe(params => {
      this.showStatistics = params['view'] === 'stats';
    });
    this.route.paramMap.subscribe(params => {
      const jobIdParam = params.get('jobId');
      if (jobIdParam) {
        this.handleJobIdChange(+jobIdParam);
      } else {
        this.focusedJob = null;
      }
    });
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.userRole = user.role;
      // Allow both ADMIN and EMPLOYER to have an employerId for job offer management
      if (user.role === 'EMPLOYER' || user.role === 'ADMIN') {
        this.employerId = user.id;
      }
    }
  }

  ngOnInit() {
    this.minDate = new Date().toISOString().split('T')[0];
    if (this.userRole === 'ADMIN' || this.userRole === 'EMPLOYER') {
      this.refreshJobOffers();
    }
    this.loadDashboardStats();
  }

  loadDashboardStats() {
    this.http.get<any>('http://localhost:8880/api/statistiques/dashboard').subscribe(stats => {
      this.stats = stats;
    });
  }

  refreshJobOffers() {
    const role = this.userRole?.toUpperCase();
    if (role === 'ADMIN' || role === 'EMPLOYER') {
      this.http.get<JobOffer[]>('http://localhost:8880/api/jobs/all-with-inactive').subscribe({
        next: offers => {
          console.log('Fetched job offers:', offers);
          this.jobOffers = offers;
          // Initial check for focused view
          const jobIdParam = this.route.snapshot.paramMap.get('jobId');
          if (jobIdParam) {
            this.handleJobIdChange(+jobIdParam);
          }
        },
        error: () => this.error = 'Failed to fetch job offers'
      });
    }
  }

  handleJobIdChange(jobId: number) {
    if (this.jobOffers.length === 0) {
      // If offers aren't loaded yet, refresh will call this again via snapshot
      return;
    }
    const job = this.jobOffers.find((o: JobOffer) => o.id === jobId);
    if (job) {
      this.focusedJob = job;
      this.fetchCandidateMatches(job.id!);
      this.showStatistics = false;
      this.showShortlisted = false;
    }
  }

  fetchCandidateMatches(jobId: number) {
    this.http.get<any[]>(`http://localhost:8880/api/jobs/${jobId}/matches`).subscribe(matches => {
      // Logic for sorting is now handled by backend, but we ensure it's stored
      this.candidateMatches[jobId] = matches;

      // If we are in focused view and this job is focused, ensure we update focusedJobCandidates indirectly
      if (this.focusedJob && this.focusedJob.id === jobId) {
        // Trigger CD or just let the getter handle it
      }
    });
  }

  toggleShortlist(matchId: number, jobId: number, currentStatus: string) {
    const newStatus = currentStatus === 'SHORTLISTED' ? 'PENDING' : 'SHORTLISTED';
    this.http.put(`http://localhost:8880/api/jobs/matches/${matchId}/status`, { status: newStatus }).subscribe({
      next: () => {
        this.success = newStatus === 'SHORTLISTED' ? 'Candidate shortlisted successfully' : 'Candidate removed from shortlist';
        if (this.selectedCandidate && this.selectedCandidate.matchId === matchId) {
          this.selectedCandidate.status = newStatus;
        }
        this.fetchCandidateMatches(jobId);
        this.fetchShortlistedCandidates(); // Refresh global list
        setTimeout(() => this.success = null, 3000);
      },
      error: () => this.error = `Failed to ${newStatus === 'SHORTLISTED' ? 'shortlist' : 'remove'} candidate`
    });
  }

  fetchShortlistedCandidates() {
    this.http.get<any[]>('http://localhost:8880/api/jobs/shortlisted-candidates').subscribe({
      next: matches => {
        this.shortlistedMatches = matches;
      },
      error: () => this.error = 'Failed to fetch shortlisted candidates'
    });
  }

  toggleShortlistedView(show: boolean) {
    this.showShortlisted = show;
    if (show) {
      this.showStatistics = false;
      this.focusedJob = null;
      this.fetchShortlistedCandidates();
    }
  }

  getScoreColor(score: number): string {
    if (score >= 70) return '#10b981'; // Green
    if (score >= 40) return '#f59e0b'; // Yellow
    return '#ef4444'; // Red
  }

  getScoreLabel(score: number): string {
    if (score >= 70) return 'Excellent';
    if (score >= 40) return 'Good';
    return 'Poor';
  }

  toggleJobDetails(job: any) {
    job.showDetails = !job.showDetails;
    if (job.showDetails && !this.candidateMatches[job.id]) {
      this.fetchCandidateMatches(job.id);
    }
  }

  openJobModal(job?: JobOffer) {
    if (job) {
      this.editingJobId = job.id || null;
      this.jobForm = { ...job };
      this.requiredSkillsString = (job.requiredSkills || []).join(', ');
      this.requiredQualificationsString = (job.requiredQualifications || []).join(', ');
      this.requiredLanguagesString = (job.requiredLanguages || []).join(', ');
      this.requiredCertificationsString = (job.requiredCertifications || []).join(', ');
    } else {
      this.editingJobId = null;
      this.jobForm = {
        title: '',
        description: '',
        active: true,
        requiredSkills: [],
        requiredQualifications: [],
        requiredLanguages: [],
        requiredCertifications: []
      };
      this.requiredSkillsString = '';
      this.requiredQualificationsString = '';
      this.requiredLanguagesString = '';
      this.requiredCertificationsString = '';
    }
    this.showJobModal = true;
    document.body.style.overflow = 'hidden';
  }

  closeJobModal() {
    this.showJobModal = false;
    document.body.style.overflow = 'auto';
  }

  createJobOffer() {
    if (!this.jobForm.title || !this.jobForm.description) {
      this.error = 'Title and Description are required';
      return;
    }
    if (!this.employerId) {
      this.error = 'User ID not found. Please log in again.';
      return;
    }
    if (this.jobForm.closingDate && this.jobForm.closingDate < this.minDate) {
      this.error = 'Closing date cannot be in the past';
      return;
    }
    this.jobForm.requiredSkills = this.requiredSkillsString.split(',').map(s => s.trim()).filter(s => s);
    this.jobForm.requiredQualifications = this.requiredQualificationsString.split(',').map(s => s.trim()).filter(s => s);
    this.jobForm.requiredLanguages = this.requiredLanguagesString.split(',').map(s => s.trim()).filter(s => s);
    this.jobForm.requiredCertifications = this.requiredCertificationsString.split(',').map(s => s.trim()).filter(s => s);

    this.http.post<JobOffer>(`http://localhost:8880/api/jobs/add?employerId=${this.employerId}`, this.jobForm).subscribe({
      next: () => {
        this.success = 'Job offer created successfully';
        this.error = null;
        this.closeJobModal();
        this.refreshJobOffers();
        setTimeout(() => this.success = null, 3500);
      },
      error: () => this.error = 'Failed to create job offer'
    });
  }

  editJobOffer(job: JobOffer) {
    this.editingJobId = job.id || null;
    this.jobForm = { ...job };
    this.jobForm.requiredSkills = job.requiredSkills ? [...job.requiredSkills] : [];
    this.jobForm.requiredQualifications = job.requiredQualifications ? [...job.requiredQualifications] : [];
    this.jobForm.requiredLanguages = job.requiredLanguages ? [...job.requiredLanguages] : [];
    this.jobForm.requiredCertifications = job.requiredCertifications ? [...job.requiredCertifications] : [];
    this.requiredSkillsString = (job.requiredSkills || []).join(', ');
    this.requiredQualificationsString = (job.requiredQualifications || []).join(', ');
    this.requiredLanguagesString = (job.requiredLanguages || []).join(', ');
    this.requiredCertificationsString = (job.requiredCertifications || []).join(', ');
  }

  updateJobOffer() {
    if (!this.editingJobId) return;

    if (this.jobForm.closingDate && this.jobForm.closingDate < this.minDate) {
      this.error = 'Closing date cannot be in the past';
      return;
    }

    this.jobForm.requiredSkills = this.requiredSkillsString.split(',').map(s => s.trim()).filter(s => s);
    this.jobForm.requiredQualifications = this.requiredQualificationsString.split(',').map(s => s.trim()).filter(s => s);
    this.jobForm.requiredLanguages = this.requiredLanguagesString.split(',').map(s => s.trim()).filter(s => s);
    this.jobForm.requiredCertifications = this.requiredCertificationsString.split(',').map(s => s.trim()).filter(s => s);

    this.http.put<JobOffer>(`http://localhost:8880/api/jobs/update/${this.editingJobId}`, this.jobForm).subscribe({
      next: () => {
        this.success = 'Job offer updated successfully';
        this.error = null;
        this.closeJobModal();
        this.refreshJobOffers();
        setTimeout(() => this.success = null, 3500);
      },
      error: () => this.error = 'Failed to update job offer'
    });
  }

  confirmDelete(job: JobOffer) {
    this.jobToDelete = job;
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.jobToDelete = null;
  }

  deleteJobOffer() {
    if (!this.jobToDelete?.id) return;
    this.http.delete(`http://localhost:8880/api/jobs/delete/${this.jobToDelete.id}`).subscribe({
      next: () => {
        this.success = 'Job offer deleted successfully';
        this.error = null;
        this.closeDeleteModal();
        this.refreshJobOffers();
        setTimeout(() => this.success = null, 3500);
      },
      error: () => {
        this.error = 'Failed to delete job offer';
        this.closeDeleteModal();
      }
    });
  }

  cancelEdit() {
    this.editingJobId = null;
    this.jobForm = {
      title: '',
      description: '',
      requiredSkills: [],
      requiredQualifications: [],
      requiredLanguages: [],
      requiredCertifications: []
    };
    this.requiredSkillsString = '';
    this.requiredQualificationsString = '';
    this.requiredLanguagesString = '';
    this.requiredCertificationsString = '';
  }

  openCandidatesModal(job: JobOffer) {
    this.selectedJobForCandidates = job;
    this.showCandidatesModal = true;
    this.rankingMode = false;
    this.fetchCandidateMatches(job.id!);
  }

  openRankingModal(job: JobOffer) {
    this.selectedJobForCandidates = job;
    this.showCandidatesModal = true;
    this.rankingMode = true;
    this.fetchCandidateMatches(job.id!);
  }

  setRankingMode(mode: boolean) {
    this.rankingMode = mode;
  }

  closeCandidatesModal() {
    this.showCandidatesModal = false;
    this.selectedJobForCandidates = null;
  }

  get candidateMatchesForSelectedJob(): any[] {
    const id = this.selectedJobForCandidates?.id;
    return (typeof id === 'number' && this.candidateMatches[id]) ? this.candidateMatches[id] : [];
  }

  selectCandidate(candidate: any) {
    this.selectedCandidate = candidate;
  }

  clearSelectedCandidate() {
    this.selectedCandidate = null;
  }

  openCVModal() {
    this.showCVModal = true;
  }

  closeCVModal() {
    this.showCVModal = false;
  }

  goBackToAllOffers() {
    this.focusedJob = null;
    this.router.navigate(['/admin']);
  }

  get focusedJobCandidates(): any[] {
    const id = this.focusedJob?.id;
    return (typeof id === 'number' && this.candidateMatches[id]) ? this.candidateMatches[id] : [];
  }
} 