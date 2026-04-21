import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';
import { CvService } from '../../services/cv.service';

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
  styleUrls: ['./admin-dashboard.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
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
  employerId: string | null = null;
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

  candidateViewMode = false;
  appliedJobs: { [jobId: number]: { applied: boolean, score?: number } | undefined } = {};

  constructor(private router: Router, private http: HttpClient, private route: ActivatedRoute, private authService: AuthService, private cvService: CvService) {
    this.route.queryParams.subscribe(params => {
      if (params['view'] === 'offers') {
        this.showShortlisted = false;
        this.showStatistics = false;
        this.candidateViewMode = false;
        this.focusedJob = null;
        this.refreshJobOffers();
      } else if (params['view'] === 'candidate-board') {
        this.showShortlisted = false;
        this.showStatistics = false;
        this.candidateViewMode = true;
        this.focusedJob = null;
        this.refreshJobOffers();
      } else {
        this.showStatistics = params['view'] === 'stats';
        this.candidateViewMode = false;
        this.refreshJobOffers();
      }
    });
    this.route.paramMap.subscribe(params => {
      const jobIdParam = params.get('jobId');
      if (jobIdParam) {
        this.handleJobIdChange(+jobIdParam);
      } else {
        this.focusedJob = null;
      }
    });

    const userInfo = this.authService.getUserInfo();
    if (userInfo) {
      console.log('User info loaded in Admin...', userInfo);
      this.employerId = userInfo.id || null;

      const roles = userInfo.roles.map((r: string) => r.toUpperCase());
      if (roles.includes('ADMIN')) {
        this.userRole = 'ADMIN';
      } else if (roles.includes('EMPLOYER')) {
        this.userRole = 'EMPLOYER';
      } else if (roles.includes('CANDIDATE')) {
        this.userRole = 'CANDIDATE';
        // Auto-enable candidate board mode if not set
        if (!this.candidateViewMode && !this.showStatistics && !this.showShortlisted) {
           this.candidateViewMode = true;
        }
      } else {
        // Fallback
        this.userRole = 'ADMIN';
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
    this.http.get<any>(`${environment.apiUrl}/api/statistiques/dashboard`).subscribe(stats => {
      this.stats = stats;
    });
  }

  refreshJobOffers() {
    const role = this.userRole?.toUpperCase() || 'CANDIDATE';
    const isPublicView = this.candidateViewMode || role === 'CANDIDATE';

    if (isPublicView) {
      this.http.get<JobOffer[]>(`${environment.apiUrl}/api/jobs/all`).subscribe({
        next: offers => {
          this.jobOffers = offers;
          if (this.employerId) {
            this.cvService.getLatestMatchesByCandidate(this.employerId).subscribe({
              next: matches => {
                matches.forEach(m => {
                  if (m.jobOffer && m.jobOffer.id) {
                    this.appliedJobs[m.jobOffer.id] = { applied: true, score: Math.round(m.matchScore || 0) };
                  }
                });
              },
              error: (err) => {
                console.log('No matches found for this candidate yet or candidate profile not created.');
                // Silently ignore 400/404 errors as it is expected for new candidates
              }
            });
          }
        },
        error: () => this.error = 'Failed to fetch active job offers'
      });
    } else if (role === 'ADMIN' || role === 'EMPLOYER') {
      this.http.get<JobOffer[]>(`${environment.apiUrl}/api/jobs/all-with-inactive`).subscribe({
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
    this.http.get<any[]>(`${environment.apiUrl}/api/jobs/${jobId}/matches`).subscribe(matches => {
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
    this.http.put(`${environment.apiUrl}/api/jobs/matches/${matchId}/status`, { status: newStatus }).subscribe({
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
    this.http.get<any[]>(`${environment.apiUrl}/api/jobs/shortlisted-candidates`).subscribe({
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

  onFileSelected(event: Event, jobId: number) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const candidateId = this.employerId; // Using stored Keycloak UUID

      if (!candidateId) {
        this.error = 'You must be logged in to apply for this job.';
        setTimeout(() => this.error = null, 5000);
        return;
      }

      this.cvService.uploadCV(file, candidateId, jobId).subscribe({
        next: (res: any) => {
          const score = res.matchScore ? Math.round(res.matchScore) : 0;
          this.appliedJobs[jobId] = { applied: true, score: score };
          this.success = `CV submitted successfully! Your compatibility match score is: ${score}%`;
          setTimeout(() => this.success = null, 5000);
        },
        error: (err: any) => {
          this.error = err.error || 'Failed to submit CV. You might have already applied to this offer.';
          setTimeout(() => this.error = null, 5000);
        }
      });
      // Reset input so they can trigger change event again if needed
      input.value = '';
    }
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

    // The API expects 'employerId' as a string/integer. Keycloak 'id' is a string UUID.
    this.http.post<JobOffer>(`${environment.apiUrl}/api/jobs/add?employerId=${this.employerId}`, this.jobForm).subscribe({
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

    this.http.put<JobOffer>(`${environment.apiUrl}/api/jobs/update/${this.editingJobId}`, this.jobForm).subscribe({
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
    this.http.delete(`${environment.apiUrl}/api/jobs/delete/${this.jobToDelete.id}`).subscribe({
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
