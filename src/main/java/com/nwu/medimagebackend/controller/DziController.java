package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.entity.FileInfo;
import com.nwu.medimagebackend.entity.FileItem;
import com.nwu.medimagebackend.service.DziService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;

/**
 * DZI（Deep Zoom Images）控制器
 * <p>
 * 负责处理与深度缩放图像(DZI)相关的HTTP请求，包括列表查询、资源访问和删除操作。
 * DZI是一种允许高性能查看和缩放大型图像的技术，常用于医学图像等大型图像的展示。
 * </p>
 * 
 * @author MedImage团队
 */
@RestController
@RequestMapping("/api/dzi")
@Slf4j
public class DziController {

    @Autowired
    private DziService dziService;

    /**
     * 获取所有DZI文件的列表
     * 
     * @return 包含所有DZI文件信息的响应
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listDziFiles() {
        log.info("请求获取DZI文件列表");
        List<FileInfo> result = dziService.listDziFiles();
        log.info("成功返回DZI文件列表，包含{}个文件夹", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 提供静态资源访问（例如DZI描述文件或tile图片）
     * <p>
     * 该接口处理对DZI描述文件(.dzi)和瓦片图像(.jpg/.png)的请求，
     * 从物理存储位置加载资源，并返回给客户端。
     * 支持路径模式匹配，允许访问任意层级的资源。
     * </p>
     * 
     * @param request HTTP请求对象，用于获取资源路径
     * @return 包含请求资源的响应
     */
    @GetMapping("/processed/**")
    public ResponseEntity<Resource> getDziFile(HttpServletRequest request) {
        try {
            // 从请求属性中获取完整的请求路径
            String restOfThePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            // 获取匹配的模式
            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            // 利用AntPathMatcher提取出/processed/后面的路径
            String relativePath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, restOfThePath);
            
            log.debug("请求DZI资源: {}", relativePath);
            Resource resource = dziService.getDziFile(relativePath);
            log.debug("成功加载DZI资源: {}", relativePath);
            
            return ResponseEntity.ok().body(resource);
        } catch (Exception e) {
            log.error("获取DZI资源文件异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取指定文件夹中的文件列表
     * 
     * @param folderName 文件夹名称
     * @return 包含文件列表的响应，如果文件夹不存在或为空则返回404
     */
    @GetMapping("/list/{folderName}")
    public ResponseEntity<List<FileItem>> listFilesInFolder(@PathVariable String folderName) {
        log.info("请求文件夹[{}]中的文件列表", folderName);
        List<FileItem> items = dziService.listFilesInFolder(folderName);
        
        if (items.isEmpty()) {
            log.warn("文件夹[{}]不存在或为空", folderName);
            return ResponseEntity.notFound().build();
        }
        
        log.info("成功获取文件夹[{}]中的{}个文件", folderName, items.size());
        return ResponseEntity.ok(items);
    }

    /**
     * 删除指定文件夹
     * 
     * @param folderName 要删除的文件夹名称
     * @return 操作结果响应
     */
    @DeleteMapping("/deleteFolder/{folderName}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String folderName) {
        log.info("请求删除文件夹: {}", folderName);
        boolean success = dziService.deleteFolder(folderName);
        
        if (success) {
            log.info("文件夹[{}]删除成功", folderName);
            return ResponseEntity.ok().build();
        } else {
            log.error("文件夹[{}]删除失败", folderName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除指定文件夹中的指定文件
     * 
     * @param folderName 文件夹名称
     * @param fileName 文件名称
     * @return 操作结果响应
     */
    @DeleteMapping("/delete/{folderName}/{fileName}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String folderName,
            @PathVariable String fileName) {
        log.info("请求删除文件: {}/{}", folderName, fileName);
        boolean success = dziService.deleteFile(folderName, fileName);
        
        if (success) {
            log.info("文件[{}/{}]删除成功", folderName, fileName);
            return ResponseEntity.ok().build();
        } else {
            log.error("文件[{}/{}]删除失败", folderName, fileName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
