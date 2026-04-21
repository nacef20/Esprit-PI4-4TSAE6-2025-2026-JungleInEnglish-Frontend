package tn.esprit.todomodule.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsTeacherDTO {
    // Level-Wide Stats
    private Map<String, Long> todosPerLevel;
    private Map<String, Double> completionRatePerLevel;
    
    // Summary Stats for Dashboard
    private long totalStudents;
    private double averageCompletionRate;
    private long totalCompletedTodos;
    private long totalPendingTodos;
    
    // Student Specific Metrics
    private List<StudentEngagementDTO> studentEngagement;
    
    // Global Trends
    private Map<String, Long> globalCompletionTrend;

    public AnalyticsTeacherDTO() {}

    // Getters and Setters
    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public double getAverageCompletionRate() { return averageCompletionRate; }
    public void setAverageCompletionRate(double averageCompletionRate) { this.averageCompletionRate = averageCompletionRate; }

    public long getTotalCompletedTodos() { return totalCompletedTodos; }
    public void setTotalCompletedTodos(long totalCompletedTodos) { this.totalCompletedTodos = totalCompletedTodos; }

    public long getTotalPendingTodos() { return totalPendingTodos; }
    public void setTotalPendingTodos(long totalPendingTodos) { this.totalPendingTodos = totalPendingTodos; }

    public Map<String, Long> getTodosPerLevel() { return todosPerLevel; }
    public void setTodosPerLevel(Map<String, Long> todosPerLevel) { this.todosPerLevel = todosPerLevel; }

    public Map<String, Double> getCompletionRatePerLevel() { return completionRatePerLevel; }
    public void setCompletionRatePerLevel(Map<String, Double> completionRatePerLevel) { this.completionRatePerLevel = completionRatePerLevel; }

    public List<StudentEngagementDTO> getStudentEngagement() { return studentEngagement; }
    public void setStudentEngagement(List<StudentEngagementDTO> studentEngagement) { this.studentEngagement = studentEngagement; }

    public Map<String, Long> getGlobalCompletionTrend() { return globalCompletionTrend; }
    public void setGlobalCompletionTrend(Map<String, Long> globalCompletionTrend) { this.globalCompletionTrend = globalCompletionTrend; }

    public static class StudentEngagementDTO {
        private String username;
        private double levelCompletionRate;
        private long personalTodosCount;
        private long overdueCount;
        private double engagementScore;

        public StudentEngagementDTO() {}

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public double getLevelCompletionRate() { return levelCompletionRate; }
        public void setLevelCompletionRate(double levelCompletionRate) { this.levelCompletionRate = levelCompletionRate; }

        public long getPersonalTodosCount() { return personalTodosCount; }
        public void setPersonalTodosCount(long personalTodosCount) { this.personalTodosCount = personalTodosCount; }

        public long getOverdueCount() { return overdueCount; }
        public void setOverdueCount(long overdueCount) { this.overdueCount = overdueCount; }

        public double getEngagementScore() { return engagementScore; }
        public void setEngagementScore(double engagementScore) { this.engagementScore = engagementScore; }
    }
}
