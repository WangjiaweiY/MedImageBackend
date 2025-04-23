package com.nwu.medimagebackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Fullnet分析响应
 * <p>
 * 用于接收Python后端的深度学习分析结果
 * </p>
 * 
 * @author MedImage团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullnetResponse {
    
    /**
     * 分析的文件名
     */
    private String filename;
    
    /**
     * 结果图像路径
     */
    @JsonProperty("result_image_path")
    private String resultImagePath;
    
    /**
     * 叠加图像路径
     */
    @JsonProperty("overlay_image_path")
    private String overlayImagePath;
    
    /**
     * 分析参数
     */
    private FullnetParameters parameters;
    
    /**
     * 分析状态
     */
    private String status;
    
    /**
     * 错误信息（如果有）
     */
    private String message;
    
    /**
     * 分析完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date analysisTime;
} 