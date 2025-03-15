package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IHCAnalysisService {

    IhcAnalysisResult analyzeImage(String folderName, String fileName) throws Exception;

    IhcAnalysisResult getAnalysisResult(String folderName, String fileName);

    List<IhcAnalysisResult> getResultsByFolder(String folderName);
}
