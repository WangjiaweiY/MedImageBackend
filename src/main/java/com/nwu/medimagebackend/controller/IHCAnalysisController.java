package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ihc")
@Slf4j
public class IHCAnalysisController {

    @Autowired
    private IHCAnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<IhcAnalysisResult> analyzeImage(
            @RequestParam("folderName") String folderName,
            @RequestParam("fileName") String fileName) {
        try {
            log.info("已接收到：" + folderName + "/" + folderName + "的免疫组化分析请求");
            IhcAnalysisResult result = analysisService.analyzeImage(folderName, fileName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/result")
    public ResponseEntity<IhcAnalysisResult> getAnalysisResult(
            @RequestParam("folderName") String folderName,
            @RequestParam("fileName") String fileName) {
        try {
            IhcAnalysisResult result = analysisService.getAnalysisResult(folderName, fileName);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            log.info("对文件:" + folderName + "/" + fileName + "进行免疫组化结果查询");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询免疫组化结果失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/resultfolder")
    public ResponseEntity<List<IhcAnalysisResult>> getResultsByFolder(
            @RequestParam("folderName") String folderName) {
        try {
            log.info("对文件夹：" + folderName + "进行免疫组化结果查询");
            List<IhcAnalysisResult> results = analysisService.getResultsByFolder(folderName);
            if (results == null || results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
