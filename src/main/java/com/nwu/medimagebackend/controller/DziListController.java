package com.nwu.medimagebackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
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
}
