package com.nwu.medimagebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    // 从配置文件中读取 uploads/dzi 的目录，默认为相对路径 "./uploads/dzi/"
    @Value("${uploads.dzi.dir:./uploads/dzi/}")
    private String uploadsDziDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "file:" 前缀表示文件系统路径，使用相对路径
        registry.addResourceHandler("/processed/**")
                .addResourceLocations("file:" + uploadsDziDir);
    }
}
