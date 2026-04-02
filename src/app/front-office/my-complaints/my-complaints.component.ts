import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Complaint } from '../../models/complaint.model';
import { ComplaintService } from '../../services/complaint.service';

@Component({
  selector: 'app-my-complaints',
  templateUrl: './my-complaints.component.html',
  styleUrls: ['./my-complaints.component.scss']
})
export class MyComplaintsComponent implements OnInit {

  complaints: Complaint[] = [];
  filteredComplaints: Complaint[] = [];
  statusFilter = '';
  loading = false;

  constructor(
    private complaintService: ComplaintService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadComplaints();
  }

  loadComplaints(): void {
    this.loading = true;
    this.complaintService.getAll().subscribe({
      next: (data) => {
        this.complaints = data;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (this.statusFilter) {
      this.filteredComplaints = this.complaints.filter(c => c.status === this.statusFilter);
    } else {
      this.filteredComplaints = [...this.complaints];
    }
  }

  onFilterStatus(status: string): void {
    this.statusFilter = status;
    this.applyFilter();
  }

  newComplaint(): void {
    this.router.navigate(['/my-complaints/new']);
  }

  viewComplaint(id: number): void {
    this.router.navigate(['/my-complaints', id]);
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'Pending';
      case 'TRAITEE': return 'Resolved';
      case 'REJETEE': return 'Rejected';
      default: return status;
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'hourglass_top';
      case 'TRAITEE': return 'check_circle';
      case 'REJETEE': return 'cancel';
      default: return 'help';
    }
  }
}

