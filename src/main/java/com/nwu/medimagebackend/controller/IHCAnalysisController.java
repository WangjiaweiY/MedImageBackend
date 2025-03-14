package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import com.nwu.medimagebackend.service.IHCAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ihc")
public class IHCAnalysisController {

    @Autowired
    private IHCAnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<IhcAnalysisResult> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            IhcAnalysisResult result = analysisService.analyzeImage(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
