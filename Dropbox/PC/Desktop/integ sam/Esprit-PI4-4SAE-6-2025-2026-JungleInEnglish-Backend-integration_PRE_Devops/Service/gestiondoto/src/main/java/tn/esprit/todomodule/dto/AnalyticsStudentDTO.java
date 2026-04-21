package tn.esprit.todomodule.dto;

import java.util.Map;

public class AnalyticsStudentDTO {
    // Personal Todos Stats
    private long personalTotal;
    private long personalCompleted;
    private long personalPending;
    private double personalCompletionRate;
    
    // Level Todos Stats (Read-Only)
    private long levelTotal;
    private long levelCompleted;
    private long levelOverdue;
    
    // Productivity Metrics
    private double averageCompletionTimeHours;
    private Map<String, Long> personalStatusBreakdown;
    private Map<String, Long> personalPriorityBreakdown;
    
    // History for Line Chart
    private Map<String, Long> completionHistory;

    public AnalyticsStudentDTO() {}

    // Getters and Setters
    public long getPersonalTotal() { return personalTotal; }
    public void setPersonalTotal(long personalTotal) { this.personalTotal = personalTotal; }

    public long getPersonalCompleted() { return personalCompleted; }
    public void setPersonalCompleted(long personalCompleted) { this.personalCompleted = personalCompleted; }

    public long getPersonalPending() { return personalPending; }
    public void setPersonalPending(long personalPending) { this.personalPending = personalPending; }

    public double getPersonalCompletionRate() { return personalCompletionRate; }
    public void setPersonalCompletionRate(double personalCompletionRate) { this.personalCompletionRate = personalCompletionRate; }

    public long getLevelTotal() { return levelTotal; }
    public void setLevelTotal(long levelTotal) { this.levelTotal = levelTotal; }

    public long getLevelCompleted() { return levelCompleted; }
    public void setLevelCompleted(long levelCompleted) { this.levelCompleted = levelCompleted; }

    public long getLevelOverdue() { return levelOverdue; }
    public void setLevelOverdue(long levelOverdue) { this.levelOverdue = levelOverdue; }

    public double getAverageCompletionTimeHours() { return averageCompletionTimeHours; }
    public void setAverageCompletionTimeHours(double averageCompletionTimeHours) { this.averageCompletionTimeHours = averageCompletionTimeHours; }

    public Map<String, Long> getPersonalStatusBreakdown() { return personalStatusBreakdown; }
    public void setPersonalStatusBreakdown(Map<String, Long> personalStatusBreakdown) { this.personalStatusBreakdown = personalStatusBreakdown; }

    public Map<String, Long> getPersonalPriorityBreakdown() { return personalPriorityBreakdown; }
    public void setPersonalPriorityBreakdown(Map<String, Long> personalPriorityBreakdown) { this.personalPriorityBreakdown = personalPriorityBreakdown; }

    public Map<String, Long> getCompletionHistory() { return completionHistory; }
    public void setCompletionHistory(Map<String, Long> completionHistory) { this.completionHistory = completionHistory; }
}
