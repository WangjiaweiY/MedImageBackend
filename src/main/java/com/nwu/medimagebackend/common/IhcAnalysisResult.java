package com.nwu.medimagebackend.common;

import java.util.Date;

public class IhcAnalysisResult {
    private Long id;
    private String imageName;
    private double positiveArea; // 正染区域像素数
    private double totalArea;    // 总像素数
    private Date analysisDate;

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
}