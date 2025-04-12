package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.entity.IhcAnalysisResult;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 免疫组化(IHC)分析控制器
 * <p>
 * 处理与免疫组化图像分析相关的HTTP请求，包括图像分析处理和结果查询。
 * 免疫组化是一种用于检测组织中特定蛋白质表达的技术，对肿瘤诊断和分类具有重要意义。
 * </p>
 * 
 * @author MedImage团队
 */
@RestController
@RequestMapping("/api/ihc")
@Slf4j
public class IHCAnalysisController {

    @Autowired
    private IHCAnalysisService analysisService;

    /**
     * 分析免疫组化图像
     * <p>
     * 对指定的免疫组化图像进行分析处理，计算阳性细胞比例等指标。
     * </p>
     * 
     * @param folderName 图像所在文件夹名称
     * @param fileName 图像文件名
     * @return 包含分析结果的响应
     */
    @PostMapping("/analyze")
    public ResponseEntity<IhcAnalysisResult> analyzeImage(
            @RequestParam("folderName") String folderName,
            @RequestParam("fileName") String fileName) {
        try {
            log.info("接收到免疫组化分析请求: 文件夹[{}], 文件[{}]", folderName, fileName);
            IhcAnalysisResult result = analysisService.analyzeImage(folderName, fileName);
            log.info("免疫组化分析完成: 文件夹[{}], 文件[{}], 阳性率: {}", 
                    folderName, fileName, result.getPositiveRatio());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("免疫组化分析失败: 文件夹[{}], 文件[{}], 错误: {}", 
                    folderName, fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取单个文件的免疫组化分析结果
     * <p>
     * 查询指定图像的免疫组化分析结果。
     * </p>
     * 
     * @param folderName 图像所在文件夹名称
     * @param fileName 图像文件名
     * @return 包含分析结果的响应，若结果不存在则返回404状态码
     */
    @GetMapping("/result")
    public ResponseEntity<IhcAnalysisResult> getAnalysisResult(
            @RequestParam("folderName") String folderName,
            @RequestParam("fileName") String fileName) {
        try {
            log.info("查询免疫组化分析结果: 文件夹[{}], 文件[{}]", folderName, fileName);
            
            IhcAnalysisResult result = analysisService.getAnalysisResult(folderName, fileName);
            if (result == null) {
                log.warn("免疫组化分析结果不存在: 文件夹[{}], 文件[{}]", folderName, fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            log.info("成功获取免疫组化分析结果: 文件夹[{}], 文件[{}], 阳性率: {}", 
                    folderName, fileName, result.getPositiveRatio());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询免疫组化分析结果失败: 文件夹[{}], 文件[{}], 错误: {}", 
                    folderName, fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取指定文件夹下所有图像的免疫组化分析结果
     * <p>
     * 查询指定文件夹中所有图像的免疫组化分析结果。
     * </p>
     * 
     * @param folderName 文件夹名称
     * @return 包含分析结果列表的响应，若无结果则返回404状态码
     */
    @GetMapping("/resultfolder")
    public ResponseEntity<List<IhcAnalysisResult>> getResultsByFolder(
            @RequestParam("folderName") String folderName) {
        try {
            log.info("查询文件夹免疫组化分析结果: 文件夹[{}]", folderName);
            
            List<IhcAnalysisResult> results = analysisService.getResultsByFolder(folderName);
            if (results == null || results.isEmpty()) {
                log.warn("文件夹中无免疫组化分析结果: 文件夹[{}]", folderName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            log.info("成功获取文件夹免疫组化分析结果: 文件夹[{}], 共{}个结果", 
                    folderName, results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("查询文件夹免疫组化分析结果失败: 文件夹[{}], 错误: {}", 
                    folderName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
