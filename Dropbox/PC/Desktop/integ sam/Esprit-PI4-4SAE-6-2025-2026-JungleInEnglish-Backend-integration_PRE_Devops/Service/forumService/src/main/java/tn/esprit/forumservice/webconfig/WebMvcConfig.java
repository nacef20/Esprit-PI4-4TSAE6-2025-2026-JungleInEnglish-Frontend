package tn.esprit.forumservice.webconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {



        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/images/**")
                    .addResourceLocations("file:///C:/pi/Esprit-PI4-4SAE-6-2025-2026-JungleInEnglish-Backend-integration_groups_back/Service/forumService/src/main/resources/imageVedioPost/");
        }
    }

