package com.nwu.medimagebackend.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将所有 /processed/** 请求映射到 uploads/dzi/ 目录下
        registry.addResourceHandler("/processed/**")
                .addResourceLocations("file:///D:/codeworkspace/medimagebackend/uploads/dzi/");
    }
}