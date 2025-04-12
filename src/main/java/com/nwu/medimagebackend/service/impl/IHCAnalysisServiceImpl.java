package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.entity.IhcAnalysisResult;
import com.nwu.medimagebackend.mapper.IHCAnalysisMapper;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import lombok.extern.slf4j.Slf4j;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class IHCAnalysisServiceImpl implements IHCAnalysisService {

    @Autowired
    private IHCAnalysisMapper mapper;

    @Value("${uploads.register.dir:D:/codeworkspace/uploads/register_results/}")
    private String ImageDir;
    
    // 后端服务基础URL，用于构建完整的资源URL
    @Value("${app.backend-url:http://localhost:8080}")
    private String backendUrl;

    @Override
    public IhcAnalysisResult analyzeImage(String folderName, String fileName) throws Exception {
        // 构造图像完整路径，假设图像存储在 ImageDir/{folderName}/registered_slides/ 目录下，后缀为 .ome.tiff
        Path imagePath = Paths.get(ImageDir, folderName, "registered_slides", fileName + ".ome.tiff");
        log.info("分析图像路径：{}", imagePath.toString());

        // 检查文件是否存在
        if (!Files.exists(imagePath)) {
            throw new IOException("文件不存在: " + imagePath.toString());
        }

        // 创建 ImageJ2 实例
        ImageJ ij = new ImageJ();

        // 使用 SCIFIO 打开图像，返回 Dataset 对象
        Dataset dataset = ij.scifio().datasetIO().open(imagePath.toString());
        if (dataset == null) {
            throw new Exception("无法打开图像文件：" + imagePath.toString());
        }

        @SuppressWarnings("unchecked")
        Img<UnsignedByteType> img = (Img<UnsignedByteType>) dataset.getImgPlus().getImg();

        long totalPixels = img.size();
        long positiveCount = 0;
        double threshold = 195.0; // 阈值

        Cursor<UnsignedByteType> cursor = img.cursor();
        while (cursor.hasNext()) {
            UnsignedByteType pixel = cursor.next();
            if (pixel.getRealDouble() >= threshold) {
                positiveCount++;
            }
        }

        // 构造免疫组化分析结果
        IhcAnalysisResult result = new IhcAnalysisResult();
        result.setImageName(fileName);
        result.setFolderName(folderName);
        result.setPositiveArea(positiveCount);
        result.setTotalArea(totalPixels);
        result.setAnalysisDate(new Date());

        if (totalPixels > 0) {
            double ratio = (positiveCount * 100.0) / totalPixels;
            // 保留两位小数
            BigDecimal ratioBD = BigDecimal.valueOf(ratio).setScale(2, RoundingMode.HALF_UP);
            result.setPositiveRatio(ratioBD);
        } else {
            result.setPositiveRatio(BigDecimal.ZERO);
        }

        mapper.updateIhcAnalysisResult(result);
        log.info("文件：" + folderName + "/" + fileName + "分析完毕");

        return result;
    }

    @Override
    public IhcAnalysisResult getAnalysisResult(String folderName, String fileName) {
        IhcAnalysisResult result = mapper.findByImageName(folderName, fileName);
        if (result != null) {
            // 查找真实的略缩图文件
            findAndSetThumbnailPath(result, folderName, fileName);
        }
        return result;
    }

    @Override
    public List<IhcAnalysisResult> getResultsByFolder(String folderName) {
        List<IhcAnalysisResult> results = mapper.findByFolderName(folderName);
        if (results != null && !results.isEmpty()) {
            for (IhcAnalysisResult result : results) {
                String fileName = result.getImageName();
                // 查找真实的略缩图文件
                findAndSetThumbnailPath(result, folderName, fileName);
            }
        }
        return results;
    }

    /**
     * 通过扫描目录，查找包含原始文件名的略缩图路径，并设置到结果对象中
     * @param result 分析结果对象
     * @param folderName 文件夹名
     * @param fileName 原始文件名
     */
    private void findAndSetThumbnailPath(IhcAnalysisResult result, String folderName, String fileName) {
        // 构建略缩图所在目录的完整路径 - 使用绝对路径
        Path thumbnailDir = Paths.get("D:/codeworkspace/uploads/register_results", folderName, folderName, "rigid_registration");
        // 使用完整URL路径，包括域名和端口
        String defaultThumbnailPath = backendUrl + "/registration-results/" + folderName + "/" + folderName + "/rigid_registration/00_" + fileName;
        
        try {
            if (Files.exists(thumbnailDir) && Files.isDirectory(thumbnailDir)) {
                log.info("查找略缩图目录: {}", thumbnailDir);
                // 列出目录中所有文件
                File[] files = thumbnailDir.toFile().listFiles();
                if (files != null) {
                    // 遍历所有文件，查找包含原始文件名的略缩图
                    for (File file : files) {
                        String thumbnailFileName = file.getName();
                        log.info("检查文件: {} 是否包含 {}", thumbnailFileName, fileName);
                        // 检查略缩图文件名是否包含原始文件名
                        if (thumbnailFileName.contains(fileName)) {
                            log.info("找到匹配的略缩图: {}", thumbnailFileName);
                            // 找到匹配的略缩图，构建完整URL路径
                            String url = backendUrl + "/registration-results/" + folderName + "/" + folderName + "/rigid_registration/" + thumbnailFileName;
                            result.setThumbnailPath(url);
                            result.setImageUrl(url); // 同时设置imageUrl字段
                            return;
                        }
                    }
                    log.warn("目录中所有文件均不匹配原文件名 {}", fileName);
                } else {
                    log.warn("目录为空或无法列出文件: {}", thumbnailDir);
                }
            } else {
                log.warn("略缩图目录不存在: {}", thumbnailDir);
            }
            
            log.warn("未找到文件 {} 对应的略缩图，使用默认路径", fileName);
            result.setThumbnailPath(defaultThumbnailPath);
            result.setImageUrl(defaultThumbnailPath); // 同时设置imageUrl字段
        } catch (Exception e) {
            log.error("查找略缩图时出错", e);
            result.setThumbnailPath(defaultThumbnailPath);
            result.setImageUrl(defaultThumbnailPath); // 同时设置imageUrl字段
        }
    }
}
