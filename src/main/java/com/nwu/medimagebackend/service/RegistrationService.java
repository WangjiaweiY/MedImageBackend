package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.common.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface RegistrationService {
    /**
     * 处理 SVS 文件上传，保持文件夹结构，并返回上传结果信息
     */
    Map<String, Object> handleSvsUpload(MultipartFile[] files) throws IOException;

    /**
     * 列出上传目录下的所有子文件夹信息
     */
    List<FileInfo> listSvsFiles();

    /**
     * 调用配准服务处理指定文件夹的配准操作
     */
    Map<String, Object> registerFolder(String folderName);
}
