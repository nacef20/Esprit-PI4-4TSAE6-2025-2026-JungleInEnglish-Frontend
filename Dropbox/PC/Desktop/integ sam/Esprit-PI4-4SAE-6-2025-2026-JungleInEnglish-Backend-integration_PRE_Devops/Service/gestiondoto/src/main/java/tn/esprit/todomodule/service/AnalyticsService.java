package tn.esprit.todomodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.todomodule.dto.AnalyticsStudentDTO;
import tn.esprit.todomodule.dto.AnalyticsTeacherDTO;
import tn.esprit.todomodule.entity.*;
import tn.esprit.todomodule.repository.TodoRepository;
import tn.esprit.todomodule.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public AnalyticsService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsStudentDTO getStudentStats(User student) {
        List<Todo> allTodos = todoRepository.findByLevelOrUser(student.getLevel(), student);
        
        List<Todo> personalTodos = allTodos.stream()
                .filter(t -> t.getUser() != null && t.getUser().getId().equals(student.getId()))
                .collect(Collectors.toList());
        
        List<Todo> levelTodos = allTodos.stream()
                .filter(t -> t.getLevel() != null && t.getLevel() == student.getLevel())
                .collect(Collectors.toList());

        long pTotal = personalTodos.size();
        long pCompleted = personalTodos.stream().filter(t -> Boolean.TRUE.equals(t.getChecked())).count();
        double pRate = pTotal == 0 ? 0 : (double) pCompleted / pTotal * 100;

        double avgHours = personalTodos.stream()
                .filter(t -> Boolean.TRUE.equals(t.getChecked()) && t.getCreatedAt() != null && t.getUpdatedAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getUpdatedAt()).toHours())
                .average()
                .orElse(0.0);

        Map<String, Long> history = personalTodos.stream()
                .filter(t -> Boolean.TRUE.equals(t.getChecked()) && t.getUpdatedAt() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        Collectors.counting()
                ));

        Map<String, Long> statusBreakdown = personalTodos.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));
        
        Map<String, Long> priorityBreakdown = personalTodos.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        AnalyticsStudentDTO dto = new AnalyticsStudentDTO();
        dto.setPersonalTotal(pTotal);
        dto.setPersonalCompleted(pCompleted);
        dto.setPersonalPending(pTotal - pCompleted);
        dto.setPersonalCompletionRate(pRate);
        dto.setLevelTotal(levelTodos.size());
        dto.setLevelCompleted(levelTodos.stream().filter(t -> Boolean.TRUE.equals(t.getChecked())).count());
        dto.setLevelOverdue(levelTodos.stream().filter(t -> !Boolean.TRUE.equals(t.getChecked()) && t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now())).count());
        dto.setAverageCompletionTimeHours(avgHours);
        dto.setPersonalStatusBreakdown(statusBreakdown);
        dto.setPersonalPriorityBreakdown(priorityBreakdown);
        dto.setCompletionHistory(history);
        
        return dto;
    }

    @Transactional(readOnly = true)
    public AnalyticsTeacherDTO getTeacherStats() {
        List<Todo> allTodos = todoRepository.findAll();
        List<User> students = userRepository.findAllByRolesContains(Role.ROLE_STUDENT);

        Map<String, Long> todosPerLevel = allTodos.stream()
                .filter(t -> t.getLevel() != null)
                .collect(Collectors.groupingBy(t -> t.getLevel().name(), Collectors.counting()));

        List<AnalyticsTeacherDTO.StudentEngagementDTO> engagement = students.stream().map(student -> {
            List<Todo> studentTodos = todoRepository.findByLevelOrUser(student.getLevel(), student);
            long levelTotal = studentTodos.stream().filter(t -> t.getLevel() != null).count();
            long levelCompleted = studentTodos.stream().filter(t -> t.getLevel() != null && Boolean.TRUE.equals(t.getChecked())).count();
            long personalCount = studentTodos.stream().filter(t -> t.getUser() != null && t.getUser().getId().equals(student.getId())).count();
            long overdue = studentTodos.stream().filter(t -> !Boolean.TRUE.equals(t.getChecked()) && t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now())).count();
            
            double rate = levelTotal == 0 ? 0 : (double) levelCompleted / levelTotal * 100;
            double score = (rate * 0.7) + (personalCount * 2.0);

            AnalyticsTeacherDTO.StudentEngagementDTO sDto = new AnalyticsTeacherDTO.StudentEngagementDTO();
            sDto.setUsername(student.getUsername());
            sDto.setLevelCompletionRate(rate);
            sDto.setPersonalTodosCount(personalCount);
            sDto.setOverdueCount(overdue);
            sDto.setEngagementScore(score);
            return sDto;
        }).collect(Collectors.toList());

        Map<String, Long> globalTrend = allTodos.stream()
                .filter(t -> Boolean.TRUE.equals(t.getChecked()) && t.getUpdatedAt() != null)
                .collect(Collectors.groupingBy(t -> t.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE), Collectors.counting()));

        long totalCompleted = allTodos.stream().filter(t -> Boolean.TRUE.equals(t.getChecked())).count();
        long totalPending = allTodos.size() - totalCompleted;
        double avgRate = allTodos.isEmpty() ? 0 : (double) totalCompleted / allTodos.size() * 100;

        AnalyticsTeacherDTO dto = new AnalyticsTeacherDTO();
        dto.setTotalStudents(students.size());
        dto.setTotalCompletedTodos(totalCompleted);
        dto.setTotalPendingTodos(totalPending);
        dto.setAverageCompletionRate(avgRate);
        
        dto.setTodosPerLevel(todosPerLevel);
        dto.setStudentEngagement(engagement);
        dto.setGlobalCompletionTrend(globalTrend);
        
        return dto;
    }
}
