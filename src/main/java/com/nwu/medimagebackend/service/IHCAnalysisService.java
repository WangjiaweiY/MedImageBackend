package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.entity.IhcAnalysisResult;

import java.util.List;

public interface IHCAnalysisService {

    IhcAnalysisResult analyzeImage(String folderName, String fileName) throws Exception;

    IhcAnalysisResult getAnalysisResult(String folderName, String fileName);

    List<IhcAnalysisResult> getResultsByFolder(String folderName);
    
    /**
     * 使用指定阈值分析免疫组化图像并生成可视化结果
     * 
     * @param folderName 文件夹名称
     * @param fileName 图像文件名
     * @param threshold 用户指定的阈值(0-255之间)
     * @return 包含分析结果和处理后图像URL的结果对象
     * @throws Exception 如果分析过程出错
     */
    IhcAnalysisResult analyzeImageWithThreshold(String folderName, String fileName, double threshold) throws Exception;
}
