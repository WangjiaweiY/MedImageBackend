package com.nwu.medimagebackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class IhcAnalysisResult {
    private Long id;

    private String folderName;

    private String imageName;

    private double positiveArea; // 正染区域像素数

    private double totalArea;    // 总像素数

    @JsonFormat(pattern = "yyyy-MM-dd-HH:mm", timezone = "GMT+8")
    private Date analysisDate;

    private BigDecimal positiveRatio;

    private String userName;

    public IhcAnalysisResult(String folderName, String fileName, String userName, Date date) {
        this.folderName = folderName;
        this.imageName = fileName;
        this.userName = userName;
        this.analysisDate = date;
        this.positiveArea = -0.0;
        this.totalArea = -0.0;
        this.positiveRatio = BigDecimal.valueOf(-0.0);
    }
}