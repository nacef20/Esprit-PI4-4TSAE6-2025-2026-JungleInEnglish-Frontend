package tn.esprit.quiz_microservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.dto.*;
import tn.esprit.quiz_microservice.repositories.AttemptAnswerRepository;
import tn.esprit.quiz_microservice.repositories.AttemptRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements IStatsService {

    private final AttemptRepository attemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO dashboard = new DashboardStatsDTO();

        // ── Quiz-level stats (avg score + pass rate per quiz) ──
        List<Object[]> rawQuizStats = attemptRepository.getAvgScorePerQuiz();
        List<QuizStatsDTO> quizStatsList = new ArrayList<>();
        long totalAttempts = 0;
        double weightedScoreSum = 0;
        long totalPassed = 0;
        double bestPct = 0;

        for (Object[] row : rawQuizStats) {
            QuizStatsDTO qs = new QuizStatsDTO();
            qs.setQuizId(((Number) row[0]).longValue());
            qs.setQuizTitle((String) row[1]);
            double avgScore = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
            double avgTotal = row[3] != null ? ((Number) row[3]).doubleValue() : 1;
            long count = ((Number) row[4]).longValue();
            long passed = ((Number) row[5]).longValue();

            qs.setAvgScore(Math.round(avgScore * 100.0) / 100.0);
            double avgPct = avgTotal > 0 ? Math.round((avgScore / avgTotal) * 10000.0) / 100.0 : 0;
            qs.setAvgPercentage(avgPct);
            qs.setTotalAttempts(count);
            qs.setPassedCount(passed);
            qs.setPassRate(count > 0 ? Math.round((passed * 10000.0) / count) / 100.0 : 0);
            quizStatsList.add(qs);

            totalAttempts += count;
            weightedScoreSum += avgPct * count;
            totalPassed += passed;
            if (avgPct > bestPct) bestPct = avgPct;
        }
        dashboard.setQuizStats(quizStatsList);
        dashboard.setTotalAttempts(totalAttempts);
        dashboard.setGlobalAvgScore(totalAttempts > 0 ? Math.round(weightedScoreSum / totalAttempts * 100.0) / 100.0 : 0);
        dashboard.setGlobalPassRate(totalAttempts > 0 ? Math.round(totalPassed * 10000.0 / totalAttempts) / 100.0 : 0);
        dashboard.setBestScore(bestPct);

        // ── Grade distribution ──
        Object[] gradeDist = attemptRepository.getGradeDistribution();
        GradeDistributionDTO gd = new GradeDistributionDTO();
        if (gradeDist != null && gradeDist.length > 0) {
            Object[] row = gradeDist;
            // Spring Data may wrap the single row in an outer array
            if (gradeDist.length == 1 && gradeDist[0] instanceof Object[]) {
                row = (Object[]) gradeDist[0];
            }
            if (row.length >= 4) {
                gd.setExcellent(row[0] != null ? ((Number) row[0]).longValue() : 0);
                gd.setGood(row[1] != null ? ((Number) row[1]).longValue() : 0);
                gd.setAverage(row[2] != null ? ((Number) row[2]).longValue() : 0);
                gd.setFailed(row[3] != null ? ((Number) row[3]).longValue() : 0);
            }
        }
        dashboard.setGradeDistribution(gd);

        // ── Most failed questions (top 5) ──
        List<Object[]> rawFailed = attemptAnswerRepository.getMostFailedQuestions();
        List<FailedQuestionDTO> failedList = new ArrayList<>();
        int limit = Math.min(rawFailed.size(), 5);
        for (int i = 0; i < limit; i++) {
            Object[] row = rawFailed.get(i);
            FailedQuestionDTO fq = new FailedQuestionDTO();
            fq.setQuestionId(((Number) row[0]).longValue());
            fq.setQuestionContent((String) row[1]);
            fq.setQuizTitle((String) row[2]);
            long total = ((Number) row[3]).longValue();
            long wrong = row[4] != null ? ((Number) row[4]).longValue() : 0;
            fq.setTotalAnswers(total);
            fq.setWrongAnswers(wrong);
            fq.setFailRate(total > 0 ? Math.round(wrong * 10000.0 / total) / 100.0 : 0);
            failedList.add(fq);
        }
        dashboard.setMostFailedQuestions(failedList);

        return dashboard;
    }
}
