package com.fluently.lessonsservice.client;


import com.fluently.lessonsservice.dto.CourseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CoursesServiceClient {

    private final WebClient webClient;

    // Read courses-service URL from application.yml
    @Value("${courses.service.url}")
    private String coursesServiceUrl;

    @Autowired
    public CoursesServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public CourseDTO getCourseById(Long courseId) {
        try {
            // ⭐ FIXED: Match your actual endpoint
            String url = coursesServiceUrl + "/api/courses/getCourse/" + courseId;
            System.out.println("🔍 Calling courses-service at: " + url);

            CourseDTO course = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(CourseDTO.class)
                    .block();

            System.out.println("✅ Course found: " + (course != null ? course.getName() : "NULL"));
            return course;

        } catch (Exception e) {
            System.err.println("❌ Error calling courses-service: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean courseExists(Long courseId) {
        CourseDTO course = getCourseById(courseId);
        return course != null;
    }
}