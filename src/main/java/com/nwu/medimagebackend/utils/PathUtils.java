package com.nwu.medimagebackend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 路径处理工具类
 * <p>
 * 提供统一的路径处理方法，包括目录创建、文件查找、路径转换等功能。
 * 集中处理路径相关的逻辑，避免重复代码，提高可维护性。
 * </p>
 * 
 * @author MedImage团队
 */
@Slf4j
public class PathUtils {

    /**
     * 确保目录存在，如果不存在则创建
     * 
     * @param directoryPath 目录路径
     * @return 目录Path对象，如创建失败则返回null
     */
    public static Path ensureDirectoryExists(String directoryPath) {
        if (!StringUtils.hasText(directoryPath)) {
            log.error("目录路径为空");
            return null;
        }
        
        try {
            Path path = Paths.get(directoryPath).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                log.info("创建目录: {}", path);
                Files.createDirectories(path);
            }
            return path;
        } catch (IOException e) {
            log.error("创建目录失败: {}, 错误: {}", directoryPath, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 查找包含指定文件名模式的文件
     * 
     * @param directoryPath 目录路径
     * @param fileNamePattern 文件名模式（包含关系）
     * @return 匹配的文件列表，如果目录不存在或为空则返回空列表
     */
    public static List<File> findMatchingFiles(String directoryPath, String fileNamePattern) {
        if (!StringUtils.hasText(directoryPath) || !StringUtils.hasText(fileNamePattern)) {
            log.warn("目录路径或文件名模式为空");
            return List.of();
        }
        
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            log.warn("目录不存在或不是目录: {}", directoryPath);
            return List.of();
        }
        
        File[] files = directory.listFiles(file -> 
            file.isFile() && file.getName().contains(fileNamePattern)
        );
        
        if (files == null || files.length == 0) {
            log.debug("未找到匹配的文件: 目录[{}], 模式[{}]", directoryPath, fileNamePattern);
            return List.of();
        }
        
        log.debug("找到匹配的文件: 目录[{}], 模式[{}], 文件数[{}]", 
                 directoryPath, fileNamePattern, files.length);
        return Arrays.asList(files);
    }
    
    /**
     * 查找目录中的第一个匹配文件
     * 
     * @param directoryPath 目录路径
     * @param fileNamePattern 文件名模式（包含关系）
     * @return 第一个匹配的文件，如果没找到则返回null
     */
    public static File findFirstMatchingFile(String directoryPath, String fileNamePattern) {
        List<File> files = findMatchingFiles(directoryPath, fileNamePattern);
        return files.isEmpty() ? null : files.get(0);
    }
    
    /**
     * 将相对路径转换为绝对路径
     * 
     * @param basePath 基础路径
     * @param relativePath 相对路径
     * @return 绝对路径字符串，如果输入无效则返回原始relativePath
     */
    public static String toAbsolutePath(String basePath, String relativePath) {
        if (!StringUtils.hasText(basePath)) {
            return relativePath;
        }
        
        if (!StringUtils.hasText(relativePath)) {
            return basePath;
        }
        
        try {
            Path base = Paths.get(basePath).toAbsolutePath().normalize();
            Path relative = Paths.get(relativePath);
            
            if (relative.isAbsolute()) {
                return relativePath;
            }
            
            return base.resolve(relative).normalize().toString();
        } catch (Exception e) {
            log.error("路径转换失败: 基础路径[{}], 相对路径[{}], 错误: {}", 
                     basePath, relativePath, e.getMessage(), e);
            return relativePath;
        }
    }
    
    /**
     * 构建URL路径，用于资源访问
     * 
     * @param baseUrl 基础URL（如：http://localhost:8080）
     * @param pathSegments 路径片段
     * @return 完整的URL路径字符串
     */
    public static String buildUrlPath(String baseUrl, String... pathSegments) {
        if (!StringUtils.hasText(baseUrl)) {
            log.warn("基础URL为空");
            return "";
        }
        
        StringBuilder sb = new StringBuilder(baseUrl);
        
        for (String segment : pathSegments) {
            if (StringUtils.hasText(segment)) {
                if (!baseUrl.endsWith("/") && !segment.startsWith("/")) {
                    sb.append("/");
                }
                sb.append(segment);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 文件扩展名（不含点号），如果没有扩展名则返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(dotIndex + 1);
    }
    
    /**
     * 获取不带扩展名的文件名
     * 
     * @param fileName 文件名
     * @return 不带扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return fileName;
        }
        
        return fileName.substring(0, dotIndex);
    }
} 