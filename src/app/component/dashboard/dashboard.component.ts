import { Component, OnInit } from '@angular/core';
import { CvService } from '../../service/cv.service';
import { CommonModule, DecimalPipe } from '@angular/common';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  standalone: true,
  imports: [CommonModule, DecimalPipe, NavbarComponent]
})
export class DashboardComponent implements OnInit {
  uploadedCV: any = null;
  extractedSkills: string[] = [];
  extractedExperiences: string[] = [];
  extractedEducation: string[] = [];
  extractedLanguages: string[] = [];
  extractedCertifications: string[] = [];
  matches: any[] = [];
  stats: any = {};
  activeJobOffers: any[] = [];
  selectedJobId: number | null = null;
  candidateName: string = '';
  candidateId: number | null = null;
  confirmationMessage: string = '';
  confirmationType: 'success' | 'error' = 'success';
  uploadedJobIds: Set<number> = new Set();

  constructor(private cvService: CvService, private authService: AuthService, private router: Router, private http: HttpClient) { }

  ngOnInit() {
    this.loadDashboardStats();
    this.fetchActiveJobOffers();
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.candidateName = user.name || '';
      this.candidateId = user.id;
      this.getLatestMatches();
      // Fetch uploaded job IDs for this user
      if (this.candidateId !== null) {
        this.cvService.getUploadedJobIdsByCandidate(this.candidateId).subscribe((ids: number[]) => {
          this.uploadedJobIds = new Set(ids);
        });
      }
    }
  }

  fetchActiveJobOffers() {
    this.http.get<any[]>('http://localhost:8880/api/jobs/all').subscribe(offers => {
      this.activeJobOffers = offers;
    });
  }

  getLatestMatches() {
    if (this.candidateId) {
      this.cvService.getLatestMatchesByCandidate(this.candidateId).subscribe(matches => {
        this.matches = matches;
      });
    }
  }

  onFileSelected(event: any, jobId: number) {
    const file = event.target.files[0];
    const userStr = localStorage.getItem('user');
    let id = null;
    if (userStr) {
      const user = JSON.parse(userStr);
      id = user.id;
    }
    if (file && id) {
      this.cvService.uploadCV(file, id, jobId).subscribe({
        next: response => {
          this.uploadedCV = response.cv;
          this.extractedSkills = response.skills || [];
          this.extractedExperiences = response.experiences || [];
          this.extractedEducation = response.education || [];
          this.extractedLanguages = response.languages || [];
          this.extractedCertifications = response.certifications || [];
          this.selectedJobId = null;
          this.confirmationMessage = 'Your CV has been uploaded successfully!';
          this.confirmationType = 'success';
          this.uploadedJobIds.add(jobId);
          this.getLatestMatches();
          setTimeout(() => this.confirmationMessage = '', 3000);
        },
        error: err => {
          if (err.status === 409) {
            this.uploadedJobIds.add(jobId);
            this.confirmationMessage = 'You have already uploaded your CV for this job offer.';
            this.confirmationType = 'error';
          } else {
            this.confirmationMessage = 'An error occurred during upload.';
            this.confirmationType = 'error';
          }
          setTimeout(() => this.confirmationMessage = '', 3000);
        }
      });
    }
  }

  openFileDialog(jobId: number) {
    this.selectedJobId = jobId;
    setTimeout(() => {
      const fileInput = document.getElementById('fileInput-' + jobId) as HTMLElement;
      if (fileInput) fileInput.click();
    }, 0);
  }

  getMatches(cvId: number) {
    this.cvService.getMatches(cvId).subscribe(matches => {
      this.matches = matches;
    });
  }

  loadDashboardStats() {
    this.cvService.getDashboardStats().subscribe(stats => {
      this.stats = stats;
    });
  }

}
