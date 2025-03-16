package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.entity.IhcAnalysisResult;

import java.util.List;

public interface IHCAnalysisService {

    IhcAnalysisResult analyzeImage(String folderName, String fileName) throws Exception;

    IhcAnalysisResult getAnalysisResult(String folderName, String fileName);

    List<IhcAnalysisResult> getResultsByFolder(String folderName);
}
