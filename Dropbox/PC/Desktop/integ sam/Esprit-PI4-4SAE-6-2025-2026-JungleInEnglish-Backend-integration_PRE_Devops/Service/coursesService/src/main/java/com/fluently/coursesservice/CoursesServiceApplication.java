package com.fluently.coursesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoursesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoursesServiceApplication.class, args);
    }
}