package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.entity.FileInfo;
import com.nwu.medimagebackend.entity.FileItem;
import org.springframework.core.io.Resource;
import java.util.List;

/**
 * DZI（Deep Zoom Images）服务接口
 * <p>
 * 定义处理深度缩放图像(DZI)的各种操作，包括列表查询、文件访问和删除等功能。
 * 该服务主要管理和操作磁盘上的DZI文件，为控制器层提供所需的业务逻辑支持。
 * </p>
 * 
 * @author MedImage团队
 */
public interface DziService {

    /**
     * 获取所有的DZI文件夹列表
     * <p>
     * 遍历DZI处理目录，查找所有的子文件夹，每个子文件夹代表一个已处理的DZI图像集。
     * </p>
     * 
     * @return 包含所有DZI文件夹信息的列表
     */
    List<FileInfo> listDziFiles();

    /**
     * 获取DZI文件
     * <p>
     * 根据提供的相对路径，从DZI处理目录中加载对应的资源文件。
     * 这些资源可以是DZI描述文件或瓦片图像文件。
     * </p>
     * 
     * @param relativePath DZI资源的相对路径
     * @return 请求的DZI资源
     * @throws Exception 如果无法加载资源
     */
    Resource getDziFile(String relativePath) throws Exception;

    /**
     * 列出指定文件夹中的所有文件
     * <p>
     * 查找指定DZI文件夹中的所有文件，并返回它们的基本信息。
     * </p>
     * 
     * @param folderName 要查询的文件夹名称
     * @return 文件夹中文件的列表
     */
    List<FileItem> listFilesInFolder(String folderName);

    /**
     * 删除指定的文件夹
     * <p>
     * 递归删除指定的DZI文件夹及其所有内容。
     * </p>
     * 
     * @param folderName 要删除的文件夹名称
     * @return 是否成功删除
     */
    boolean deleteFolder(String folderName);

    /**
     * 删除指定文件夹中的指定文件
     * <p>
     * 从指定的DZI文件夹中删除特定的文件或子目录。
     * </p>
     * 
     * @param folderName 文件夹名称
     * @param fileName 要删除的文件或子目录名
     * @return 是否成功删除
     */
    boolean deleteFile(String folderName, String fileName);
}
