package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.entity.FileInfo;
import com.nwu.medimagebackend.entity.FileItem;
import com.nwu.medimagebackend.service.DziService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DziServiceimpl implements DziService {

    // 从配置文件中读取 uploads/dzi 目录的路径，默认值为相对路径 "./uploads/dzi/"
    @Value("${uploads.dzi.dir:./uploads/dzi/}")
    private String dziUploadDir;

    @Override
    public List<FileInfo> listDziFiles() {
        List<FileInfo> result = new ArrayList<>();
        File baseDir = new File(dziUploadDir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return result;
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
        return result;
    }

    @Override
    public Resource getDziFile(String relativePath) throws Exception {
        // 构造文件系统中的路径
        Path filePath = Paths.get(dziUploadDir).resolve(relativePath).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        } else {
            throw new Exception("File not found: " + relativePath);
        }
    }

    @Override
    public List<FileItem> listFilesInFolder(String folderName) {
        List<FileItem> items = new ArrayList<>();
        File folder = new File(dziUploadDir, folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            return items;
        }
        // 获取文件夹下的所有子项（文件和目录）
        File[] subItems = folder.listFiles();
        if (subItems != null) {
            for (File f : subItems) {
                items.add(new FileItem(f.getName(), f.isDirectory()));
            }
        }
        return items;
    }

    @Override
    public boolean deleteFolder(String folderName) {
        try {
            Path baseDir = Paths.get(dziUploadDir).toAbsolutePath().normalize();
            Path targetDirPath = baseDir.resolve(folderName).normalize();
            // 防止目录穿越攻击
            if (!targetDirPath.startsWith(baseDir)) {
                log.warn("尝试删除不在目录内的文件夹: {}", targetDirPath);
                return false;
            }
            File targetDir = targetDirPath.toFile();
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                return false;
            }
            return deleteDirectoryRecursively(targetDir);
        } catch (Exception e) {
            log.error("删除文件夹 {} 异常: {}", folderName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteFile(String folderName, String fileName) {
        try {
            Path baseDir = Paths.get(dziUploadDir).toAbsolutePath().normalize();
            Path targetPath = baseDir.resolve(folderName).resolve(fileName).normalize();
            log.info("删除路径: {}", targetPath.toString());
            // 防止目录穿越攻击
            if (!targetPath.startsWith(baseDir)) {
                log.warn("尝试删除不在目录内的文件: {}", targetPath);
                return false;
            }
            File target = targetPath.toFile();
            // 此处按照原逻辑，要求目标存在且为目录
            if (!target.exists() || !target.isDirectory()) {
                return false;
            }
            return deleteDirectoryRecursively(target);
        } catch (Exception e) {
            log.error("删除文件 {}/{} 异常: {}", folderName, fileName, e.getMessage());
            return false;
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
}
