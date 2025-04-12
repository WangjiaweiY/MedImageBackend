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
    private Date uploadsDate;

    @JsonFormat(pattern = "yyyy-MM-dd-HH:mm", timezone = "GMT+8")
    private Date analysisDate;

    private BigDecimal positiveRatio;

    private String userName;
    
    private String thumbnailPath;
    
    // 添加新字段，用于前端显示图像
    private String imageUrl;

    public IhcAnalysisResult(String folderName, String fileName, String userName, Date date) {
        this.folderName = folderName;
        this.imageName = fileName;
        this.userName = userName;
        this.uploadsDate = date;
        this.positiveArea = -0.0;
        this.totalArea = -0.0;
        this.positiveRatio = BigDecimal.valueOf(-0.0);
    }
}