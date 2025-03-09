package com.nwu.medimagebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/dzi")
@CrossOrigin(origins = "*")
@Slf4j
public class DziController {

    // 从配置文件中读取 uploads/dzi 目录的路径，默认值为相对路径 "./uploads/dzi/"
    @Value("${uploads.dzi.dir:./uploads/dzi/}")
    private String dziUploadDir;

    // 定义一个简单的 POJO 用于返回文件信息
    public static class FileInfo {
        private String folderName;
        private List<String> fileNames;

        public FileInfo(String folderName, List<String> fileNames) {
            this.folderName = folderName;
            this.fileNames = fileNames;
        }

        public FileInfo(String folderName) {
            this.folderName = folderName;
        }
        public String getFolderName() {
            return folderName;
        }
        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }
        public List<String> getFileNames() {
            return fileNames;
        }
        public void setFileNames(List<String> fileNames) {
            this.fileNames = fileNames;
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listDziFiles() {
        List<FileInfo> result = new ArrayList<>();
        File baseDir = new File(dziUploadDir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return ResponseEntity.ok(result);
        }
        // 遍历 uploads/dzi 下的每个子文件夹
        File[] subDirs = baseDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                List<String> fileNames = new ArrayList<>();
                File[] files = subDir.listFiles(File::isFile);
                if (files != null) {
                    for (File f : files) {
                        fileNames.add(f.getName());
                    }
                }
                result.add(new FileInfo(subDir.getName(), fileNames));
            }
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 提供静态资源访问的接口（例如 DZI 描述文件或 tile 图片）
     */
    @GetMapping("/processed/**")
    public ResponseEntity<Resource> getDziFile(HttpServletRequest request) {
        try {
            // 从请求属性中获取完整的请求路径
            String restOfThePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            // 获取匹配的模式
            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            // 利用 AntPathMatcher 提取出 /processed/ 后面的路径（如 "20250215/test_files0/0_0.jpeg"）
            String fileName = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, restOfThePath);
            // 构造文件系统中的路径
            Path filePath = Paths.get(dziUploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok().body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list/{folderName}")
    public ResponseEntity<List<FileItem>> listFilesInFolder(@PathVariable String folderName) {
        List<FileItem> items = new ArrayList<>();
        File folder = new File(dziUploadDir, folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            return ResponseEntity.notFound().build();
        }
        // 获取文件夹下的所有子项（文件和目录）
        File[] subItems = folder.listFiles();
        if (subItems != null) {
            for (File f : subItems) {
                items.add(new FileItem(f.getName(), f.isDirectory()));
            }
        }
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/deleteFolder/{folderName}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String folderName) {
        try {
            Path baseDir = Paths.get(dziUploadDir).toAbsolutePath().normalize();
            Path targetDirPath = baseDir.resolve(folderName).normalize();
            if (!targetDirPath.startsWith(baseDir)) {
                log.warn("尝试删除不在目录内的文件夹: {}", targetDirPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            File targetDir = targetDirPath.toFile();
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                return ResponseEntity.notFound().build();
            }
            if (deleteDirectoryRecursively(targetDir)) {
                log.info(folderName + "删除成功");
                return ResponseEntity.ok().build();
            } else {
                log.error("删除文件夹失败: {}", targetDirPath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("删除文件夹异常: {} {}", folderName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @DeleteMapping("/delete/{folderName}/{fileName}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String folderName,
            @PathVariable String fileName) {
        try {
            // 构造 uploads/dzi 目录的绝对路径
            Path baseDir = Paths.get(dziUploadDir).toAbsolutePath().normalize();
            // 构造目标目录的绝对路径
            Path targetDirPath = baseDir.resolve(folderName).resolve(fileName).normalize();
            log.info(targetDirPath.toString());
            // 检查目标目录是否位于允许删除的目录下，防止目录穿越攻击
            if (!targetDirPath.startsWith(baseDir)) {
                log.warn("尝试删除不在目录内的目录: {}", targetDirPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            File targetDir = targetDirPath.toFile();
            // 检查目录是否存在且确实为目录
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                return ResponseEntity.notFound().build();
            }
            // 递归删除目录及其所有内容
            if (deleteDirectoryRecursively(targetDir)) {
                log.info(fileName + "已被删除");
                return ResponseEntity.ok().build();
            } else {
                log.error("删除目录失败: {}", targetDirPath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("删除目录异常: {} {}", folderName + "/" + fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 递归删除目录及其所有内容
     */
    private boolean deleteDirectoryRecursively(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDirectoryRecursively(child)) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }



    // 定义一个返回项的 POJO 类，用于区分文件和目录
    public static class FileItem {
        private String name;
        private boolean directory; // true 表示目录，false 表示文件

        public FileItem(String name, boolean directory) {
            this.name = name;
            this.directory = directory;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isDirectory() {
            return directory;
        }

        public void setDirectory(boolean directory) {
            this.directory = directory;
        }
    }

}
