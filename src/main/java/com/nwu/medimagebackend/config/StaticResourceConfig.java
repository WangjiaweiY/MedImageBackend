package com.nwu.medimagebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    // 从配置文件中读取 uploads/dzi 的目录，默认为相对路径 "./uploads/dzi/"
    @Value("${uploads.dzi.dir:./uploads/dzi/}")
    private String uploadsDziDir;
    
    // 配准结果目录路径配置，默认为相对路径 "../uploads/register_results/"
    @Value("${uploads.register.dir:../uploads/register_results/}")
    private String registrationResultsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "file:" 前缀表示文件系统路径，使用相对路径
        registry.addResourceHandler("/processed/**")
                .addResourceLocations("file:" + uploadsDziDir);
        
        // 配准结果静态资源映射，构建绝对路径
        // 根据实际环境，构建uploads目录的绝对路径
        // D:\codeworkspace\medimagebackend的上级是D:\codeworkspace，
        // uploads目录应该在D:\codeworkspace下
        String uploadsPath = "D:/codeworkspace/uploads/register_results/";
        
        registry.addResourceHandler("/registration-results/**")
                .addResourceLocations("file:" + uploadsPath);
    }
}
