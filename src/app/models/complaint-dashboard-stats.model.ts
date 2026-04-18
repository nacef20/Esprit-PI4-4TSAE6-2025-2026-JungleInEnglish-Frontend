export type ComplaintStatsPeriod = 'DAY' | 'MONTH' | 'YEAR';

export interface ComplaintPeriodCount {
  period: string;
  count: number;
}

export interface ComplaintDashboardStats {
  totalComplaints: number;
  complaintsByStatus: {
    EN_ATTENTE?: number;
    TRAITEE?: number;
    REJETEE?: number;
    [key: string]: number | undefined;
  };
  complaintsByPeriod: ComplaintPeriodCount[];
  averageProcessingHours: number;
  unresolvedRate: number;
}
