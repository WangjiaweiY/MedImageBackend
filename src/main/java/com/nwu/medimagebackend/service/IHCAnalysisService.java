package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import org.springframework.web.multipart.MultipartFile;

public interface IHCAnalysisService {

    IhcAnalysisResult analyzeImage(MultipartFile file) throws Exception;
}
