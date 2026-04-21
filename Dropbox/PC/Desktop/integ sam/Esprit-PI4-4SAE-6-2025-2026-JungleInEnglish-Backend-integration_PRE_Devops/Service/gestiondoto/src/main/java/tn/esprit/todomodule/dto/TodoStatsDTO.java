package tn.esprit.todomodule.dto;

import java.util.Map;

public class TodoStatsDTO {
    // Redundant fields to cover all possible frontend expected names
    private long totalTodos;
    private long total;
    
    private double completed;
    private double completedPercentage;
    private long completedCount;
    
    private long pending;
    private long pendingCount;
    
    private long highPriority;
    private long highPriorityCount;
    
    private double overdue;
    private double overduePercentage;
    private long overdueCount;

    private Map<String, Long> statusBreakdown;
    private Map<String, Long> priorityDistribution;
    private Map<String, Long> priorityBreakdown;

    public TodoStatsDTO() {}

    public TodoStatsDTO(long totalTodos, double completedPercentage, double overduePercentage, 
                        long highPriorityCount, long pendingCount,
                        Map<String, Long> statusBreakdown, Map<String, Long> priorityDistribution) {
        this.totalTodos = totalTodos;
        this.total = totalTodos;
        this.completed = completedPercentage;
        this.completedPercentage = completedPercentage;
        this.completedCount = Math.round((completedPercentage / 100.0) * totalTodos);
        this.pending = pendingCount;
        this.pendingCount = pendingCount;
        this.highPriority = highPriorityCount;
        this.highPriorityCount = highPriorityCount;
        this.overdue = overduePercentage;
        this.overduePercentage = overduePercentage;
        this.overdueCount = Math.round((overduePercentage / 100.0) * totalTodos);
        this.statusBreakdown = statusBreakdown;
        this.priorityDistribution = priorityDistribution;
        this.priorityBreakdown = priorityDistribution;
    }

    // Getters and Setters for ALL fields to ensure serialization
    public long getTotalTodos() { return totalTodos; }
    public void setTotalTodos(long totalTodos) { this.totalTodos = totalTodos; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public double getCompleted() { return completed; }
    public void setCompleted(double completed) { this.completed = completed; }
    public double getCompletedPercentage() { return completedPercentage; }
    public void setCompletedPercentage(double completedPercentage) { this.completedPercentage = completedPercentage; }
    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }
    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }
    public long getPendingCount() { return pendingCount; }
    public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }
    public long getHighPriority() { return highPriority; }
    public void setHighPriority(long highPriority) { this.highPriority = highPriority; }
    public long getHighPriorityCount() { return highPriorityCount; }
    public void setHighPriorityCount(long highPriorityCount) { this.highPriorityCount = highPriorityCount; }
    public double getOverdue() { return overdue; }
    public void setOverdue(double overdue) { this.overdue = overdue; }
    public double getOverduePercentage() { return overduePercentage; }
    public void setOverduePercentage(double overduePercentage) { this.overduePercentage = overduePercentage; }
    public long getOverdueCount() { return overdueCount; }
    public void setOverdueCount(long overdueCount) { this.overdueCount = overdueCount; }
    public Map<String, Long> getStatusBreakdown() { return statusBreakdown; }
    public void setStatusBreakdown(Map<String, Long> statusBreakdown) { this.statusBreakdown = statusBreakdown; }
    public Map<String, Long> getPriorityDistribution() { return priorityDistribution; }
    public void setPriorityDistribution(Map<String, Long> priorityDistribution) { this.priorityDistribution = priorityDistribution; }
    public Map<String, Long> getPriorityBreakdown() { return priorityBreakdown; }
    public void setPriorityBreakdown(Map<String, Long> priorityBreakdown) { this.priorityBreakdown = priorityBreakdown; }
}
