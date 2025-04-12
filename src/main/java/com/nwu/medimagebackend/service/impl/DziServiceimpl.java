package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.entity.FileInfo;
import com.nwu.medimagebackend.entity.FileItem;
import com.nwu.medimagebackend.service.DziService;
import com.nwu.medimagebackend.mapper.DziMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * DZI服务实现类
 * <p>
 * 负责处理深度缩放图像(DZI)的各种操作，包括列表查询、文件访问和删除等功能。
 * 该服务主要管理和操作磁盘上的DZI文件，为控制器层提供所需的业务逻辑支持。
 * </p>
 *
 * @author MedImage团队
 */
@Service
@Slf4j
public class DziServiceimpl implements DziService {

    /**
     * DZI上传目录路径，从配置文件中读取，默认值为相对路径 "./uploads/dzi/"
     */
    @Value("${uploads.dzi.dir:./uploads/dzi/}")
    private String dziUploadDir;

    /**
     * DZI数据访问对象
     */
    @Autowired
    private DziMapper dziMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listDziFiles() {
        log.info("开始获取DZI文件列表，路径: {}", dziUploadDir);
        List<FileInfo> result = new ArrayList<>();
        File baseDir = new File(dziUploadDir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            log.warn("DZI上传目录不存在或不是一个目录: {}", dziUploadDir);
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
                log.debug("找到DZI文件夹: {}, 包含{}个文件", subDir.getName(), fileNames.size());
            }
        }
        log.info("DZI文件列表获取完成，共找到{}个文件夹", result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getDziFile(String relativePath) throws Exception {
        // 构造文件系统中的路径
        Path filePath = Paths.get(dziUploadDir).resolve(relativePath).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        } else {
            log.warn("请求的DZI资源不存在: {}", filePath);
            throw new Exception("文件未找到: " + relativePath);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileItem> listFilesInFolder(String folderName) {
        log.info("开始获取文件夹[{}]中的文件列表", folderName);
        List<FileItem> items = new ArrayList<>();
        File folder = new File(dziUploadDir, folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            log.warn("指定的文件夹不存在或不是一个目录: {}", folder.getAbsolutePath());
            return items;
        }
        // 获取文件夹下的所有子项（文件和目录）
        File[] subItems = folder.listFiles();
        if (subItems != null) {
            for (File f : subItems) {
                items.add(new FileItem(f.getName(), f.isDirectory()));
                log.debug("找到{}[{}]", f.isDirectory() ? "目录" : "文件", f.getName());
            }
        }
        log.info("文件夹[{}]中文件列表获取完成，共找到{}个项目", folderName, items.size());
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFolder(String folderName) {
        log.info("开始删除文件夹: {}", folderName);
        try {
            Path baseDir = Paths.get(dziUploadDir).toAbsolutePath().normalize();
            Path targetDirPath = baseDir.resolve(folderName).normalize();
            
            // 防止目录穿越攻击
            if (!targetDirPath.startsWith(baseDir)) {
                log.warn("安全警告：尝试删除不在目录内的文件夹: {}", targetDirPath);
                return false;
            }
            
            File targetDir = targetDirPath.toFile();
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                log.warn("要删除的文件夹不存在或不是一个目录: {}", targetDirPath);
                return false;
            }
            
            boolean result = deleteDirectoryRecursively(targetDir);
            if (result) {
                log.info("文件夹[{}]删除成功", folderName);
            } else {
                log.warn("文件夹[{}]删除失败", folderName);
            }
            return result;
        } catch (Exception e) {
            log.error("删除文件夹[{}]时发生异常: {}", folderName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFile(String folderName, String fileName) {
        log.info("开始删除文件: {}/{}", folderName, fileName);
        try {
            Path baseDir = Paths.get(dziUploadDir).toAbsolutePath().normalize();
            Path targetPath = baseDir.resolve(folderName).resolve(fileName).normalize();
            log.debug("删除路径: {}", targetPath);
            
            // 防止目录穿越攻击
            if (!targetPath.startsWith(baseDir)) {
                log.warn("安全警告：尝试删除不在目录内的文件: {}", targetPath);
                return false;
            }
            
            File target = targetPath.toFile();
            // 此处按照原逻辑，要求目标存在且为目录
            if (!target.exists() || !target.isDirectory()) {
                log.warn("要删除的文件不存在或不是一个目录: {}", targetPath);
                return false;
            }
            
            boolean fileDeleted = deleteDirectoryRecursively(target);
            if (fileDeleted) {
                // 删除数据库中对应folderName的数据
                int rows = dziMapper.deleteByFilename(folderName, fileName);
                log.info("数据库删除成功，影响行数: {}", rows);
                log.info("文件[{}/{}]删除成功", folderName, fileName);
            } else {
                log.warn("文件[{}/{}]删除失败", folderName, fileName);
            }
            return fileDeleted;
        } catch (Exception e) {
            log.error("删除文件[{}/{}]时发生异常: {}", folderName, fileName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 递归删除目录及其所有内容
     * 
     * @param dir 要删除的目录
     * @return 是否成功删除
     */
    private boolean deleteDirectoryRecursively(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDirectoryRecursively(child)) {
                        log.warn("无法删除子项: {}", child.getAbsolutePath());
                        return false;
                    }
                }
            }
        }
        
        boolean result = dir.delete();
        if (!result && dir.exists()) {
            log.warn("无法删除: {}", dir.getAbsolutePath());
        }
        return result;
    }
}
