package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            IhcAnalysisResult result = analysisService.analyzeImage(folderName, fileName);
            log.info("已接收到：" + folderName + "/" + folderName + "的免疫组化分析请求");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
