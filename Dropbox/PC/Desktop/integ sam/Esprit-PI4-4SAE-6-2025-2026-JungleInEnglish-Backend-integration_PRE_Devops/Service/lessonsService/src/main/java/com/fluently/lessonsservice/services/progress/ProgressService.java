package com.fluently.lessonsservice.services.progress;

import com.fluently.lessonsservice.dto.progress.CourseCompletionRateDTO;
import com.fluently.lessonsservice.dto.progress.CourseProgressSummaryDTO;
import com.fluently.lessonsservice.dto.progress.LessonDifficultyDTO;
import com.fluently.lessonsservice.dto.progress.LessonDifficultyRowDTO;
import com.fluently.lessonsservice.dto.reco.CourseLiteDTO;
import com.fluently.lessonsservice.entities.Lesson;
import com.fluently.lessonsservice.entities.LessonAttachment;
import com.fluently.lessonsservice.entities.progress.AttachmentProgress;
import com.fluently.lessonsservice.entities.progress.LessonProgress;
import com.fluently.lessonsservice.entities.progress.ProgressStatus;
import com.fluently.lessonsservice.repositories.LessonAttachmentRepository;
import com.fluently.lessonsservice.repositories.LessonRepository;
import com.fluently.lessonsservice.repositories.progress.AttachmentProgressRepository;
import com.fluently.lessonsservice.repositories.progress.LessonProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProgressService implements IProgressService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonAttachmentRepository lessonAttachmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private AttachmentProgressRepository attachmentProgressRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void startLesson(String userId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));

        Long courseId = lesson.getCourseId();

        LessonProgress lp = lessonProgressRepository
                .findByUserIdAndLessonIdAndCourseId(userId, lessonId, courseId)
                .orElse(null);

        if (lp == null) {
            lp = new LessonProgress();
            lp.setUserId(userId);
            lp.setLessonId(lessonId);
            lp.setCourseId(courseId);
            lp.setStatus(ProgressStatus.IN_PROGRESS);
            lp.setLastActivityAt(LocalDateTime.now());
            lessonProgressRepository.save(lp);
            return;
        }

        if (lp.getStatus() != ProgressStatus.COMPLETED) {
            lp.setStatus(ProgressStatus.IN_PROGRESS);
            lp.setLastActivityAt(LocalDateTime.now());
            lessonProgressRepository.save(lp);
        }
    }

    @Override
    public void completeLesson(String userId, Long lessonId, Long timeSpentSeconds) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));

        Long courseId = lesson.getCourseId();

        LessonProgress lp = lessonProgressRepository
                .findByUserIdAndLessonIdAndCourseId(userId, lessonId, courseId)
                .orElse(null);

        if (lp == null) {
            lp = new LessonProgress();
            lp.setUserId(userId);
            lp.setLessonId(lessonId);
            lp.setCourseId(courseId);
        }

        lp.setStatus(ProgressStatus.COMPLETED);
        lp.setCompletedAt(LocalDateTime.now());
        lp.setLastActivityAt(LocalDateTime.now());

        if (timeSpentSeconds != null && timeSpentSeconds >= 0) {
            long old = lp.getTimeSpentSeconds() == null ? 0L : lp.getTimeSpentSeconds();
            lp.setTimeSpentSeconds(Math.max(old, timeSpentSeconds));
        }

        lessonProgressRepository.save(lp);
    }

    @Override
    public void completeAttachment(String userId, Long attachmentId) {
        LessonAttachment att = lessonAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));

        AttachmentProgress existing = attachmentProgressRepository
                .findByUserIdAndAttachmentId(userId, attachmentId)
                .orElse(null);

        if (existing != null) {
            return;
        }

        Lesson lesson = lessonRepository.findById(att.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found for attachment"));

        AttachmentProgress ap = new AttachmentProgress();
        ap.setUserId(userId);
        ap.setAttachmentId(attachmentId);
        ap.setLessonId(att.getLessonId());
        ap.setCourseId(lesson.getCourseId());
        ap.setCompletedAt(LocalDateTime.now());

        attachmentProgressRepository.save(ap);
    }

    @Override
    public CourseProgressSummaryDTO getCourseSummary(String userId, Long courseId) {
        long totalLessons = lessonRepository.countByCourseId(courseId);
        long completedLessons = lessonProgressRepository
                .countByCourseAndUserAndStatus(courseId, userId, ProgressStatus.COMPLETED);

        double percent = totalLessons > 0 ? (completedLessons * 100.0) / totalLessons : 0.0;

        List<Long> completedLessonIds = lessonProgressRepository.findCompletedLessonIds(courseId, userId);

        Lesson nextLesson;
        if (completedLessonIds == null || completedLessonIds.isEmpty()) {
            nextLesson = lessonRepository.findFirstByCourseIdOrderByOrderIndexAsc(courseId).orElse(null);
        } else {
            nextLesson = lessonRepository.findNextNotCompleted(courseId, completedLessonIds).orElse(null);
        }

        Integer unlockedMaxOrderIndex;
        Long nextLessonId;

        if (nextLesson == null) {
            unlockedMaxOrderIndex = lessonRepository.findMaxOrderIndexByCourse(courseId);
            nextLessonId = null;
        } else {
            unlockedMaxOrderIndex = nextLesson.getOrderIndex();
            nextLessonId = nextLesson.getId();
        }

        CourseProgressSummaryDTO dto = new CourseProgressSummaryDTO();
        dto.setCourseId(courseId);
        dto.setTotalLessons(totalLessons);
        dto.setCompletedLessons(completedLessons);
        dto.setCompletionPercent(Math.round(percent * 100.0) / 100.0);
        dto.setNextLessonId(nextLessonId);
        dto.setUnlockedOrderIndexMax(unlockedMaxOrderIndex == null ? 0 : unlockedMaxOrderIndex);

        return dto;
    }

    public void recordLessonHeartbeat(Long lessonId, String userId, Long courseId, long deltaSeconds) {
        long safeDelta = Math.min(deltaSeconds, 60);

        LessonProgress lp = lessonProgressRepository
                .findByUserIdAndLessonIdAndCourseId(userId, lessonId, courseId)
                .orElseGet(() -> {
                    LessonProgress created = new LessonProgress();
                    created.setUserId(userId);
                    created.setLessonId(lessonId);
                    created.setCourseId(courseId);
                    created.setStatus(ProgressStatus.IN_PROGRESS);
                    created.setTimeSpentSeconds(0L);
                    created.setLastActivityAt(LocalDateTime.now());
                    return created;
                });

        if (lp.getStatus() == null || lp.getStatus() == ProgressStatus.NOT_STARTED) {
            lp.setStatus(ProgressStatus.IN_PROGRESS);
        }

        long current = lp.getTimeSpentSeconds() == null ? 0L : lp.getTimeSpentSeconds();
        lp.setTimeSpentSeconds(current + safeDelta);
        lp.setLastActivityAt(LocalDateTime.now());

        lessonProgressRepository.save(lp);
    }

    @Override
    public List<LessonDifficultyDTO> getLessonDifficultyByCourse(Long courseId) {
        List<LessonDifficultyRowDTO> rows = lessonProgressRepository.getDifficultyAggByCourse(courseId);

        var scored = rows.stream().map(r -> {
            double avg = r.getAvgTimeSeconds() == null ? 0.0 : r.getAvgTimeSeconds();
            long started = r.getStartedCount() == null ? 0L : r.getStartedCount();
            long completed = r.getCompletedCount() == null ? 0L : r.getCompletedCount();

            double rate = started == 0 ? 0.0 : (completed * 1.0) / started;
            double score = avg * (1.0 - rate);

            return new Object() {
                final LessonDifficultyRowDTO row = r;
                final double completionRate = rate;
                final double avgTime = avg;
                final double s = score;
            };
        }).collect(Collectors.toList());

        List<Long> sortedByScore = scored.stream()
                .sorted(Comparator.comparingDouble(x -> -x.s))
                .map(x -> x.row.getLessonId())
                .collect(Collectors.toList());

        Set<Long> hardLessons = sortedByScore.stream().limit(1).collect(Collectors.toSet());
        Set<Long> easyLessons = sortedByScore.stream()
                .skip(Math.max(0, sortedByScore.size() - 1))
                .collect(Collectors.toSet());

        return scored.stream().map(x -> {
            String label;
            if (hardLessons.contains(x.row.getLessonId())) {
                label = "HARD";
            } else if (easyLessons.contains(x.row.getLessonId())) {
                label = "EASY";
            } else {
                label = "MEDIUM";
            }

            return new LessonDifficultyDTO(
                    x.row.getLessonId(),
                    x.row.getStartedCount(),
                    x.row.getCompletedCount(),
                    x.completionRate,
                    x.avgTime,
                    label
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<CourseCompletionRateDTO> getCourseCompletionRates() {
        List<Object[]> rows = lessonProgressRepository.getCourseCompletionAgg();

        return rows.stream().map(r -> {
            Long courseId = (Long) r[0];
            long started = (Long) r[1];
            long completed = (Long) r[2];
            double rate = started == 0 ? 0.0 : (completed * 1.0) / started;

            return new CourseCompletionRateDTO(courseId, started, completed, rate);
        }).toList();
    }

    @Override
    public void enrollCourse(String userId, Long courseId) {
        boolean already = lessonProgressRepository.findMyCourseIds(userId).contains(courseId);
        if (already) return;

        LessonProgress lp = new LessonProgress();
        lp.setUserId(userId);
        lp.setCourseId(courseId);
        lp.setLessonId(0L);
        lp.setStatus(ProgressStatus.IN_PROGRESS);
        lp.setLastActivityAt(LocalDateTime.now());
        lp.setTimeSpentSeconds(0L);

        lessonProgressRepository.save(lp);
    }

    @Override
    public List<Long> getMyCourseIds(String userId) {
        return lessonProgressRepository.findMyCourseIds(userId);
    }

    @Override
    public List<Long> getTop3RecommendedCourseIds(String userId) {
        List<Long> myIds = lessonProgressRepository.findMyCourseIds(userId);
        if (myIds == null) myIds = List.of();

        String url = "http://localhost:8222/api/courses/getAllcourses";

        ResponseEntity<List<CourseLiteDTO>> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CourseLiteDTO>>() {}
        );

        List<CourseLiteDTO> all = resp.getBody();
        if (all == null || all.isEmpty()) return List.of();

        var examKeywords = List.of(
                "toefl", "ielts", "bac", "a1", "a2", "b1", "b2",
                "speaking", "writing", "reading", "listening",
                "grammar", "vocabulary", "vocab", "pronunciation"
        );

        Set<String> wanted = new java.util.HashSet<>();

        for (CourseLiteDTO c : all) {
            if (c.getId() != null && myIds.contains(c.getId())) {
                String text = ((c.getName() == null ? "" : c.getName()) + " " +
                        (c.getDescription() == null ? "" : c.getDescription()))
                        .toLowerCase();

                for (String k : examKeywords) {
                    if (text.contains(k)) wanted.add(k);
                }
            }
        }

        class Scored {
            Long id;
            int score;
            Scored(Long id, int score) {
                this.id = id;
                this.score = score;
            }
        }

        List<Scored> scored = new java.util.ArrayList<>();

        for (CourseLiteDTO c : all) {
            if (c.getId() == null) continue;
            if (myIds.contains(c.getId())) continue;

            String text = ((c.getName() == null ? "" : c.getName()) + " " +
                    (c.getDescription() == null ? "" : c.getDescription()))
                    .toLowerCase();

            int s = 0;
            for (String k : wanted) {
                if (text.contains(k)) s++;
            }
            scored.add(new Scored(c.getId(), s));
        }

        List<Long> first = scored.stream()
                .filter(x -> x.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .map(x -> x.id)
                .distinct()
                .limit(3)
                .toList();

        List<Long> result = new java.util.ArrayList<>(first);

        if (result.size() < 3) {
            List<Long> fill = scored.stream()
                    .sorted((a, b) -> Integer.compare(b.score, a.score))
                    .map(x -> x.id)
                    .filter(id -> !result.contains(id))
                    .distinct()
                    .limit(3 - result.size())
                    .toList();

            result.addAll(fill);
        }

        return result;
    }
}