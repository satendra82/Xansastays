package com.project.xansastays.GuestMaster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the external upload directory as a static-resource location.
 *
 * Files saved to  C:/xansa-uploads/profiles/abc.jpg
 * become reachable at  /uploads/profiles/abc.jpg
 *
 * This is necessary because Spring Boot's built-in classpath:/static/ folder
 * is baked into the JAR at compile time — files written there at runtime are
 * never picked up by the static resource handler.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";

        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations("file:///" + location);
    }
}