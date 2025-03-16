package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.entity.FileInfo;
import com.nwu.medimagebackend.entity.FileItem;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DziService {
    /**
     * 获取 DZI 目录下各子文件夹的文件列表
     */
    List<FileInfo> listDziFiles();

    /**
     * 根据相对路径获取静态资源
     */
    Resource getDziFile(String relativePath) throws Exception;

    /**
     * 获取指定文件夹下的所有子项（文件或文件夹）
     */
    List<FileItem> listFilesInFolder(String folderName);

    /**
     * 删除指定的文件夹
     */
    boolean deleteFolder(String folderName);

    /**
     * 删除指定文件夹下的子目录或文件（本示例中，删除操作针对的是目录，业务逻辑与删除文件夹类似）
     */
    boolean deleteFile(String folderName, String fileName);
}
