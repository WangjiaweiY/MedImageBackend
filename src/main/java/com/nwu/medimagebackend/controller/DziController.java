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

@RestController
@RequestMapping("/api/dzi")
@CrossOrigin(origins = "*")
@Slf4j
public class DziController {

    @Autowired
    private DziService dziService;

    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listDziFiles() {
        List<FileInfo> result = dziService.listDziFiles();
        log.info("已拉取dzi文件列表");
        return ResponseEntity.ok(result);
    }

    /**
     * 提供静态资源访问（例如 DZI 描述文件或 tile 图片）
     */
    @GetMapping("/processed/**")
    public ResponseEntity<Resource> getDziFile(HttpServletRequest request) {
        try {
            // 从请求属性中获取完整的请求路径
            String restOfThePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            // 获取匹配的模式
            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            // 利用 AntPathMatcher 提取出 /processed/ 后面的路径
            String relativePath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, restOfThePath);
            Resource resource = dziService.getDziFile(relativePath);
            return ResponseEntity.ok().body(resource);
        } catch (Exception e) {
            log.error("获取文件异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list/{folderName}")
    public ResponseEntity<List<FileItem>> listFilesInFolder(@PathVariable String folderName) {
        List<FileItem> items = dziService.listFilesInFolder(folderName);
        if (items.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/deleteFolder/{folderName}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String folderName) {
        boolean success = dziService.deleteFolder(folderName);
        if (success) {
            log.info("文件夹 {} 删除成功", folderName);
            return ResponseEntity.ok().build();
        } else {
            log.error("删除文件夹 {} 失败", folderName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{folderName}/{fileName}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String folderName,
            @PathVariable String fileName) {
        boolean success = dziService.deleteFile(folderName, fileName);
        if (success) {
            log.info("文件 {} 下的 {} 删除成功", folderName, fileName);
            return ResponseEntity.ok().build();
        } else {
            log.error("删除文件 {} 下的 {} 失败", folderName, fileName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
