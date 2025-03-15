package com.nwu.medimagebackend.common;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

public class IhcAnalysisResult {
    private Long id;

    private String folderName;

    private String imageName;
    private double positiveArea; // 正染区域像素数
    private double totalArea;    // 总像素数
    @JsonFormat(pattern = "yyyy-MM-dd-HH:mm", timezone = "GMT+8")
    private Date analysisDate;

    private BigDecimal positiveRatio;

    // getter & setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getImageName() {
        return imageName;
    }
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    public double getPositiveArea() {
        return positiveArea;
    }
    public void setPositiveArea(double positiveArea) {
        this.positiveArea = positiveArea;
    }
    public double getTotalArea() {
        return totalArea;
    }
    public void setTotalArea(double totalArea) {
        this.totalArea = totalArea;
    }
    public Date getAnalysisDate() {
        return analysisDate;
    }
    public void setAnalysisDate(Date analysisDate) {
        this.analysisDate = analysisDate;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public BigDecimal getPositiveRatio() {
        return positiveRatio;
    }

    public void setPositiveRatio(BigDecimal positiveRatio) {
        this.positiveRatio = positiveRatio;
    }
}