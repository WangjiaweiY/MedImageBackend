package com.nwu.medimagebackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class IhcAnalysisResult {
    private Long id;

    private String folderName;

    private String imageName;

    private double positiveArea; // 正染区域像素数

    private double totalArea;    // 总像素数

    @JsonFormat(pattern = "yyyy-MM-dd-HH:mm", timezone = "GMT+8")
    private Date analysisDate;

    private BigDecimal positiveRatio;

}