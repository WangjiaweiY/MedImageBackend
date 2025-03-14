package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import com.nwu.medimagebackend.mapper.IHCAnalysisMapper;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

@Service
public class IHCAnalysisServiceImpl implements IHCAnalysisService {

    @Autowired
    private IHCAnalysisMapper mapper;

    @Override
    public IhcAnalysisResult analyzeImage(MultipartFile file) throws Exception {
        // 将上传文件保存为临时文件
        File tempFile = File.createTempFile("upload", ".tif");
        file.transferTo(tempFile);

        // 创建 ImageJ2 实例
        ImageJ ij = new ImageJ();

        // 使用 SciJava IO 打开 TIFF 文件，得到 Dataset 对象
        Dataset dataset = ij.scifio().datasetIO().open(tempFile.getAbsolutePath());
        // 从 Dataset 中获取 ImgPlus，再获得 Img
        // 此处假设图像数据转换为 FloatType，如有需要，可调用 ij.convert() 进行类型转换
        Img<FloatType> img = (Img<FloatType>) dataset.getImgPlus().getImg();

        // 计算图像总像素数（支持多维图像，这里假设图像为二维）
        long totalPixels = 1;
//        for (long d : img.dimensions()) {
//            totalPixels *= d;
//        }

        // 定义阈值（根据实际需要调整阈值数值）
        float threshold = 128f;
        long positiveCount = 0;

        // 利用 ImgLib2 的 Cursor 遍历像素
        Cursor<FloatType> cursor = img.cursor();
        while (cursor.hasNext()) {
            FloatType pixel = cursor.next();
            if (pixel.get() >= threshold) {
                positiveCount++;
            }
        }

        // 构建分析结果对象
        IhcAnalysisResult result = new IhcAnalysisResult();
        result.setImageName(file.getOriginalFilename());
        result.setPositiveArea(positiveCount);
        result.setTotalArea(totalPixels);
        result.setAnalysisDate(new Date());

        // 保存结果到数据库
        mapper.insert(result);

        // 删除临时文件
        tempFile.delete();

        return result;
    }
}
