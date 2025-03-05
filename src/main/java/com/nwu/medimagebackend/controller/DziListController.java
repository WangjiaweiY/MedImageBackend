package com.nwu.medimagebackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/dzi")
@CrossOrigin(origins = "*")
public class DziListController {

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
    @GetMapping("/processed/{fileName:.+}")
    public ResponseEntity<Resource> getDziFile(@PathVariable String fileName) {
        try {
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

    /**
     * 获取指定日期文件夹下的文件列表
     * GET /api/dzi/list/{folderName}
     */
    @GetMapping("/list/{folderName}")
    public ResponseEntity<List<String>> listFilesInFolder(@PathVariable String folderName) {
        List<String> fileNames = new ArrayList<>();
        File folder = new File(dziUploadDir, folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            return ResponseEntity.notFound().build();
        }
        File[] files = folder.listFiles(File::isFile);
        if (files != null) {
            for (File f : files) {
                fileNames.add(f.getName());
            }
        }
        return ResponseEntity.ok(fileNames);
    }
}
