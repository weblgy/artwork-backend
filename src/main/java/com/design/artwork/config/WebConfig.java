package com.design.artwork.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ⚠️注意：如果你是 Windows，路径前加 file:
        // 如果你是 Mac，路径前也加 file:
        // 这里必须和你下面 Controller 设置的路径对应
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/MyProject/images/");
    }
}