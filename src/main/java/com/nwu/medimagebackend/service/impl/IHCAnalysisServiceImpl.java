package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import com.nwu.medimagebackend.mapper.IHCAnalysisMapper;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import lombok.extern.slf4j.Slf4j;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.table.ResultsTable;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
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

    @Value("../uploads/register_results/")
    private String ImageDir;

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

        // 保存结果到数据库
        mapper.insert(result);

        log.info("文件：" + folderName + "/" + fileName + "分析完毕");

        return result;
    }



    @Override
    public IhcAnalysisResult getAnalysisResult(String folderName, String fileName) {
        return mapper.findByImageName(folderName, fileName);
    }

    @Override
    public List<IhcAnalysisResult> getResultsByFolder(String folderName) {
        return mapper.findByFolderName(folderName);
    }
}
