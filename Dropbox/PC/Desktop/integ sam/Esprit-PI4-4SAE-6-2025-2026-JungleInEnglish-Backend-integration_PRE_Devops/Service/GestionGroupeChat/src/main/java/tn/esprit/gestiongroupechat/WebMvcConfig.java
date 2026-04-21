package tn.esprit.gestiongroupechat;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // On expose le dossier "uploads/images" via URL
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:/C:/jungle-in-english-backend-integration1/Service/GestionGroupeChat/uploads/images/");
    }
}
