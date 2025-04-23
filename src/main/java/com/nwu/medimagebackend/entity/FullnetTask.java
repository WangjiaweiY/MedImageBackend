package com.nwu.medimagebackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Fullnet异步分析任务
 * <p>
 * 用于存储和跟踪异步图像分析任务的状态
 * </p>
 * 
 * @author MedImage团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullnetTask {
    
    /**
     * 任务ID，使用UUID生成
     */
    private String id;
    
    /**
     * 分析的文件名
     */
    private String filename;
    
    /**
     * 实际处理的文件名（含后缀）
     */
    private String actualFilename;
    
    /**
     * 任务状态：PENDING（等待中）, PROCESSING（处理中）, COMPLETED（已完成）, FAILED（失败）
     */
    private String status;
    
    /**
     * 进度描述
     */
    private String progress;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
    
    /**
     * 任务创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;
    
    /**
     * 任务完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completedTime;
    
    /**
     * 关联的分析结果ID（完成后）
     */
    private Long resultId;
} 