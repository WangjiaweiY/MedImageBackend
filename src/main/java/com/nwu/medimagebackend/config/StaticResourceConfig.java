package com.nwu.medimagebackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源配置类
 * 
 * 负责配置静态资源映射，将物理路径映射到URL路径
 * 支持DZI图像资源和配准结果图像的访问
 * 
 * @author MedImage团队
 */
@Slf4j
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    /**
     * DZI图像的存储目录，从配置文件中读取，默认为"./uploads/dzi/"
     */
    @Value("${uploads.dzi.dir:../uploads/dzi/}")
    private String uploadsDziDir;
    
    /**
     * 配准结果图像的存储目录，从配置文件中读取，默认为"../uploads/register_results/"
     */
    @Value("${uploads.register.dir:../uploads/register_results/}")
    private String registrationResultsDir;
    
    /**
     * 处理后的图像存储目录
     */
    @Value("${uploads.processed.dir:../uploads/processed/}")
    private String processedImagesDir;
    
    /**
     * 配准结果的绝对路径，为了确保路径一致性
     */
    private String registrationResultsAbsolutePath;
    
    /**
     * 处理后图像的绝对路径
     */
    private String processedImagesAbsolutePath;

    /**
     * 初始化配置，检查目录存在性并记录日志
     */
    @PostConstruct
    public void init() {
        log.info("初始化静态资源配置");
        log.info("DZI图像目录: {}", uploadsDziDir);
        log.info("配准结果目录: {}", registrationResultsDir);
        log.info("处理后图像目录: {}", processedImagesDir);
        
        // 检查目录是否存在，创建日志记录
        checkDirectory(uploadsDziDir);
        checkDirectory(registrationResultsDir);
        checkDirectory(processedImagesDir);
        
        // 设置固定的绝对路径，确保一致性
        registrationResultsAbsolutePath = "D:/codeworkspace/uploads/register_results/";
        log.info("配准结果绝对路径: {}", registrationResultsAbsolutePath);
        
        // 设置处理后图像的绝对路径
        processedImagesAbsolutePath = "D:/codeworkspace/uploads/processed/";
        log.info("处理后图像绝对路径: {}", processedImagesAbsolutePath);
    }
    
    /**
     * 检查目录是否存在，记录日志
     * 
     * @param directory 目录路径
     */
    private void checkDirectory(String directory) {
        Path path = Paths.get(directory);
        if (Files.exists(path)) {
            log.info("目录已存在: {}", path.toAbsolutePath());
        } else {
            log.warn("目录不存在: {}", path.toAbsolutePath());
        }
    }

    /**
     * 配置资源处理器，将物理路径映射到URL路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置DZI图像资源映射
        // configDziResourceMapping(registry);

        // 配置配准结果图像资源映射
        configRegistrationResultsMapping(registry);
        
        // 配置处理后图像资源映射
        configProcessedImagesMapping(registry);
    }
    
    /**
     * 配置DZI图像资源映射
     * 
     * @param registry 资源处理器注册表
     */
//    private void configDziResourceMapping(ResourceHandlerRegistry registry) {
//        String dziLocation = "file:" + uploadsDziDir;
//        log.info("注册DZI资源映射: /processed/** -> {}", dziLocation);
//        registry.addResourceHandler("/processed/**")
//                .addResourceLocations(dziLocation);
//    }
    
    /**
     * 配置配准结果图像资源映射
     * 
     * @param registry 资源处理器注册表
     */
    private void configRegistrationResultsMapping(ResourceHandlerRegistry registry) {
        // 使用固定的绝对路径，确保一致性
        String location = "file:" + registrationResultsAbsolutePath;
        log.info("注册配准结果资源映射: /registration-results/** -> {}", location);
        registry.addResourceHandler("/registration-results/**")
                .addResourceLocations(location);
    }
    
    /**
     * 配置处理后图像资源映射
     * 
     * @param registry 资源处理器注册表
     */
    private void configProcessedImagesMapping(ResourceHandlerRegistry registry) {
        String location = "file:" + processedImagesAbsolutePath;
        log.info("注册处理后图像资源映射: /processed-images/** -> {}", location);
        registry.addResourceHandler("/processed-images/**")
                .addResourceLocations(location);
    }
}
