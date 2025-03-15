package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import com.nwu.medimagebackend.mapper.IHCAnalysisMapper;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import lombok.extern.slf4j.Slf4j;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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
        // Construct the file path
        Path imagePath = Paths.get(ImageDir, folderName, "registered_slides", fileName+".ome.tiff");
        log.info(imagePath.toString());

        // Check if the file exists
        if (!Files.exists(imagePath)) {
            throw new IOException("File not found: " + imagePath.toString());
        }

        // Create ImageJ instance
        ImageJ ij = new ImageJ();

//        // Open the image file
//        Dataset dataset = ij.scifio().datasetIO().open(imagePath.toString());
//        Img<FloatType> img = (Img<FloatType>) dataset.getImgPlus().getImg();
//
//        // Calculate total pixels
//        long totalPixels = img.size();
//
//        // Define threshold
//        float threshold = 128f;
//        long positiveCount = 0;
//
//        // Iterate over pixels
//        Cursor<FloatType> cursor = img.cursor();
//        while (cursor.hasNext()) {
//            FloatType pixel = cursor.next();
//            if (pixel.get() >= threshold) {
//                positiveCount++;
//            }
//        }

        // Build analysis result
        IhcAnalysisResult result = new IhcAnalysisResult();
        result.setImageName(fileName);
        result.setFolderName(folderName);
        result.setPositiveArea(123);
        result.setTotalArea(12345);
        result.setAnalysisDate(new Date());

        // Save result to database
        mapper.insert(result);

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
