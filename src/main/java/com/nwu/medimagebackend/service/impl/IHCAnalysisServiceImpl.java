package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.entity.IhcAnalysisResult;
import com.nwu.medimagebackend.mapper.IHCAnalysisMapper;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import com.nwu.medimagebackend.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * 免疫组化分析服务实现类
 * <p>
 * 提供对免疫组化(IHC)图像的分析功能，包括图像读取、处理、结果存储和查询。
 * </p>
 * 
 * @author MedImage团队
 */
@Service
@Slf4j
public class IHCAnalysisServiceImpl implements IHCAnalysisService {

    @Autowired
    private IHCAnalysisMapper mapper;

    /**
     * 图像目录路径
     */
    @Value("${uploads.register.dir}")
    private String imageDir;
    
    /**
     * 后端服务URL，用于构建资源访问路径
     */
    @Value("${app.backend-url}")
    private String backendUrl;
    
    /**
     * 处理后的图像存储目录
     */
    @Value("${uploads.processed.dir:../uploads/processed/}")
    private String processedImagesDir;
    
    /**
     * 服务初始化
     */
    @PostConstruct
    public void init() {
        log.info("初始化IHC分析服务");
        log.info("图像目录: {}", imageDir);
        log.info("后端服务URL: {}", backendUrl);
        log.info("处理后图像目录: {}", processedImagesDir);
        
        // 确保目录存在
        PathUtils.ensureDirectoryExists(imageDir);
        PathUtils.ensureDirectoryExists(processedImagesDir);
    }

    /**
     * 分析免疫组化图像
     * 
     * @param folderName 文件夹名称
     * @param fileName 文件名
     * @return 分析结果
     * @throws Exception 如果分析过程出错
     */
    @Override
    public IhcAnalysisResult analyzeImage(String folderName, String fileName) throws Exception {
        // 构造图像完整路径
        String slidesDir = PathUtils.toAbsolutePath(imageDir, folderName + "/registered_slides");
        Path imagePath = Paths.get(slidesDir, fileName + ".ome.tiff");
        log.info("分析图像路径：{}", imagePath);

        // 检查文件是否存在
        if (!Files.exists(imagePath)) {
            throw new IOException("文件不存在: " + imagePath.toString());
        }

        // 创建ImageJ2实例并打开图像
        ImageJ ij = new ImageJ();
        Dataset dataset = ij.scifio().datasetIO().open(imagePath.toString());
        if (dataset == null) {
            throw new Exception("无法打开图像文件: " + imagePath);
        }

        // 分析图像
        @SuppressWarnings("unchecked")
        Img<UnsignedByteType> img = (Img<UnsignedByteType>) dataset.getImgPlus().getImg();
        
        // 计算阈值统计数据
        AnalysisResult analysis = calculateDefaultThresholdStatistics(img);

        // 构造免疫组化分析结果
        IhcAnalysisResult result = createAnalysisResult(folderName, fileName, analysis);

        // 保存分析结果
        mapper.updateIhcAnalysisResult(result);
        log.info("文件: {}/{} 分析完毕, 阳性率: {}", folderName, fileName, result.getPositiveRatio());

        return result;
    }

    /**
     * 根据文件夹名和文件名获取分析结果
     * 
     * @param folderName 文件夹名称
     * @param fileName 文件名
     * @return 分析结果，如果未找到则返回null
     */
    @Override
    public IhcAnalysisResult getAnalysisResult(String folderName, String fileName) {
        IhcAnalysisResult result = mapper.findByImageName(folderName, fileName);
        if (result != null) {
            // 查找对应的略缩图并设置URL路径
            findAndSetThumbnailPath(result, folderName, fileName);
        }
        return result;
    }

    /**
     * 获取指定文件夹的所有分析结果
     * 
     * @param folderName 文件夹名称
     * @return 分析结果列表
     */
    @Override
    public List<IhcAnalysisResult> getResultsByFolder(String folderName) {
        List<IhcAnalysisResult> results = mapper.findByFolderName(folderName);
        if (results != null && !results.isEmpty()) {
            for (IhcAnalysisResult result : results) {
                // 为每个结果设置略缩图路径
                findAndSetThumbnailPath(result, result.getFolderName(), result.getImageName());
            }
        }
        return results;
    }

    /**
     * 计算图像的阈值统计数据
     * 
     * @param img 图像数据
     * @return 分析结果包含总像素数和阳性像素数
     */
    private AnalysisResult calculateThresholdStatistics(Img<UnsignedByteType> img) {
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
        
        return new AnalysisResult(totalPixels, positiveCount);
    }
    
    /**
     * 创建分析结果对象
     * 
     * @param folderName 文件夹名称
     * @param fileName 文件名
     * @param analysis 分析统计数据
     * @return 分析结果对象
     */
    private IhcAnalysisResult createAnalysisResult(String folderName, String fileName, AnalysisResult analysis) {
        IhcAnalysisResult result = new IhcAnalysisResult();
        result.setImageName(fileName);
        result.setFolderName(folderName);
        result.setPositiveArea(analysis.positiveCount);
        result.setTotalArea(analysis.totalPixels);
        result.setAnalysisDate(new Date());

        if (analysis.totalPixels > 0) {
            double ratio = (analysis.positiveCount * 100.0) / analysis.totalPixels;
            // 保留两位小数
            BigDecimal ratioBD = BigDecimal.valueOf(ratio).setScale(2, RoundingMode.HALF_UP);
            result.setPositiveRatio(ratioBD);
        } else {
            result.setPositiveRatio(BigDecimal.ZERO);
        }
        
        return result;
    }

    /**
     * 查找并设置略缩图路径
     * 
     * @param result 分析结果对象
     * @param folderName 文件夹名称
     * @param fileName 文件名
     */
    private void findAndSetThumbnailPath(IhcAnalysisResult result, String folderName, String fileName) {
        // 构建略缩图目录路径
        String thumbnailDirPath = PathUtils.toAbsolutePath(
                imageDir, folderName + "/" + folderName + "/rigid_registration");
        
        // 构建资源访问的基础URL路径
        String resourceBasePath = "/registration-results/" + folderName + "/" + folderName + "/rigid_registration/";
        
        // 默认缩略图文件名和URL路径
        String defaultThumbnailName = "00_" + fileName;
        String defaultThumbnailUrl = PathUtils.buildUrlPath(backendUrl, resourceBasePath, defaultThumbnailName);
        
        try {
            // 使用PathUtils工具类查找匹配的文件
            File matchingFile = PathUtils.findFirstMatchingFile(thumbnailDirPath, fileName);
            
            if (matchingFile != null) {
                // 找到匹配的缩略图文件
                String thumbnailFileName = matchingFile.getName();
                log.debug("找到匹配的略缩图: {}", thumbnailFileName);
                
                // 构建完整URL路径
                String thumbnailUrl = PathUtils.buildUrlPath(backendUrl, resourceBasePath, thumbnailFileName);
                result.setThumbnailPath(thumbnailUrl);
                result.setImageUrl(thumbnailUrl);
                
                // 记录日志
                log.debug("设置缩略图URL: {}", thumbnailUrl);
            } else {
                // 未找到匹配的略缩图文件，使用默认路径
                log.warn("未找到文件 {} 对应的略缩图，使用默认路径: {}", fileName, defaultThumbnailUrl);
                result.setThumbnailPath(defaultThumbnailUrl);
                result.setImageUrl(defaultThumbnailUrl);
            }
        } catch (Exception e) {
            log.error("查找略缩图时出错: {}", e.getMessage(), e);
            result.setThumbnailPath(defaultThumbnailUrl);
            result.setImageUrl(defaultThumbnailUrl);
        }
    }
    
    /**
     * 使用指定阈值分析免疫组化图像并生成可视化结果
     * 
     * @param folderName 文件夹名称
     * @param fileName 文件名
     * @param threshold 用户指定的阈值(0-255之间)
     * @return 分析结果和带有阳性区域高亮的图像URL
     * @throws Exception 如果分析过程出错
     */
    @Override
    public IhcAnalysisResult analyzeImageWithThreshold(String folderName, String fileName, double threshold) throws Exception {
        // 确保阈值在有效范围内(0-255)
        threshold = Math.max(0, Math.min(255, threshold));
        
        // 构造图像完整路径
        String slidesDir = PathUtils.toAbsolutePath(imageDir, folderName + "/registered_slides");
        Path imagePath = Paths.get(slidesDir, fileName + ".ome.tiff");
        log.info("使用阈值[{}]分析图像: {}", threshold, imagePath);

        // 检查文件是否存在
        if (!Files.exists(imagePath)) {
            throw new IOException("文件不存在: " + imagePath.toString());
        }

        // 创建ImageJ2实例并打开图像
        ImageJ ij = new ImageJ();
        Dataset dataset = ij.scifio().datasetIO().open(imagePath.toString());
        if (dataset == null) {
            throw new Exception("无法打开图像文件: " + imagePath);
        }

        // 分析图像
        @SuppressWarnings("unchecked")
        Img<UnsignedByteType> img = (Img<UnsignedByteType>) dataset.getImgPlus().getImg();
        
        // 计算阈值统计数据
        AnalysisResult analysis = calculateThresholdStatistics(img, threshold);
        
        // 生成可视化结果图像(将大于阈值的区域标红)
        String processedImagePath = generateProcessedImage(img, threshold, folderName, fileName);
        
        // 构造免疫组化分析结果
        IhcAnalysisResult result = createAnalysisResult(folderName, fileName, analysis);
        
        // 设置处理后的图像URL
        String processedImageUrl = PathUtils.buildUrlPath(backendUrl, "/processed-images/", 
                folderName + "/" + fileName + "_threshold_" + (int)threshold + ".png");
        result.setImageUrl(processedImageUrl);
        
        // 保存分析结果
        mapper.updateIhcAnalysisResult(result);
        log.info("文件: {}/{} 使用阈值[{}]分析完毕, 阳性率: {}", 
                folderName, fileName, threshold, result.getPositiveRatio());

        return result;
    }
    
    /**
     * 使用指定阈值计算图像的阈值统计数据
     * 
     * @param img 图像数据
     * @param threshold 阈值
     * @return 分析结果包含总像素数和阳性像素数
     */
    private AnalysisResult calculateThresholdStatistics(Img<UnsignedByteType> img, double threshold) {
        long totalPixels = img.size();
        long positiveCount = 0;
        
        Cursor<UnsignedByteType> cursor = img.cursor();
        while (cursor.hasNext()) {
            UnsignedByteType pixel = cursor.next();
            if (pixel.getRealDouble() >= threshold) {
                positiveCount++;
            }
        }
        
        return new AnalysisResult(totalPixels, positiveCount);
    }
    
    /**
     * 生成处理后的图像文件，将大于阈值的区域标红
     * 
     * @param img 原始图像
     * @param threshold 阈值
     * @param folderName 文件夹名称
     * @param fileName 文件名
     * @return 处理后图像的文件路径
     * @throws IOException 如果图像处理或保存失败
     */
    private String generateProcessedImage(Img<UnsignedByteType> img, double threshold, 
                                         String folderName, String fileName) throws IOException {
        // 创建相同尺寸的彩色图像
        long[] dimensions = new long[img.numDimensions()];
        img.dimensions(dimensions);
        
        // 创建处理后图像的存储目录
        String outputDirPath = PathUtils.toAbsolutePath(processedImagesDir, folderName);
        PathUtils.ensureDirectoryExists(outputDirPath);
        
        // 处理后图像的文件名
        String outputFileName = fileName + "_threshold_" + (int)threshold + ".png";
        Path outputPath = Paths.get(outputDirPath, outputFileName);
        
        // 先创建一个临时数组存储所有像素值
        int width = (int)dimensions[0];
        int height = (int)dimensions[1];
        double[][] pixelValues = new double[width][height];
        
        // 第一次遍历：读取所有像素值
        Cursor<UnsignedByteType> firstCursor = img.localizingCursor();
        while (firstCursor.hasNext()) {
            UnsignedByteType pixel = firstCursor.next();
            int[] position = new int[img.numDimensions()];
            firstCursor.localize(position);
            
            int x = position[0];
            int y = position[1];
            
            if (x < width && y < height) {
                pixelValues[x][y] = pixel.getRealDouble();
            }
        }
        
        // 创建Java AWT BufferedImage用于保存结果 - 使用RGB类型
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // 第二次遍历：创建输出图像
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = pixelValues[x][y];
                int rgb;
                
                if (value >= threshold) {
                    // 大于阈值的区域标红 - 半透明红色，保留一些原始灰度信息
                    int gray = (int)value;
                    int red = Math.min(255, 255);
                    int green = Math.min(255, gray / 3);
                    int blue = Math.min(255, gray / 3);
                    rgb = new Color(red, green, blue).getRGB();
                } else {
                    // 其他区域保持原灰度
                    int gray = (int)value;
                    rgb = new Color(gray, gray, gray).getRGB();
                }
                
                bufferedImage.setRGB(x, y, rgb);
            }
        }
        
        // 保存图像
        ImageIO.write(bufferedImage, "PNG", outputPath.toFile());
        log.info("生成处理后图像: {}", outputPath);
        
        return outputPath.toString();
    }
    
    /**
     * 计算图像的阈值统计数据，使用默认阈值(195.0)
     * 
     * @param img 图像数据
     * @return 分析结果包含总像素数和阳性像素数
     */
    private AnalysisResult calculateDefaultThresholdStatistics(Img<UnsignedByteType> img) {
        return calculateThresholdStatistics(img, 195.0); // 使用默认阈值195.0
    }
    
    /**
     * 内部类：分析结果统计数据
     */
    private static class AnalysisResult {
        final long totalPixels;
        final long positiveCount;
        
        AnalysisResult(long totalPixels, long positiveCount) {
            this.totalPixels = totalPixels;
            this.positiveCount = positiveCount;
        }
    }
}
