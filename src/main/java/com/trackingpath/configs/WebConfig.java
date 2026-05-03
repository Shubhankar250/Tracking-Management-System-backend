package com.trackingpath.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:/home/upload/");
        registry.addResourceHandler("/snapshots/**")
        .addResourceLocations("file:/home/dashcam_upload/snapshot/");
        registry.addResourceHandler("/chatUploads/**")
        .addResourceLocations("file:/home/chatUploads/");
        registry.addResourceHandler("/soundUploads/**")
        .addResourceLocations("file:/home/soundUploads/");
    }
    
}
