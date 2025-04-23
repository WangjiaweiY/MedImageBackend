package com.nwu.medimagebackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Fullnet分析结果
 * <p>
 * 用于存储到数据库的深度学习分析结果
 * </p>
 * 
 * @author MedImage团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullnetResult {
    
    /**
     * 结果ID
     */
    private Long id;
    
    /**
     * 分析的文件名
     */
    private String filename;
    
    /**
     * 结果图像路径
     */
    private String resultImagePath;
    
    /**
     * 叠加图像路径
     */
    private String overlayImagePath;
    
    /**
     * 细胞数量
     */
    private Integer cellCount;
    
    /**
     * 细胞面积
     */
    private Integer cellArea;
    
    /**
     * 总面积
     */
    private Integer totalArea;
    
    /**
     * 细胞比例(%)
     */
    private Double cellRatio;
    
    /**
     * 平均细胞大小
     */
    private Double avgCellSize;
    
    /**
     * 分析完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date analysisTime;
    
    /**
     * 关联的异步任务ID
     */
    private String taskId;
    
    /**
     * 从响应对象创建结果对象
     * 
     * @param response Fullnet分析响应
     * @param taskId 异步任务ID
     * @return 结果对象
     */
    public static FullnetResult fromResponse(FullnetResponse response, String taskId) {
        FullnetResult result = new FullnetResult();
        result.setFilename(response.getFilename());
        result.setResultImagePath(response.getResultImagePath());
        result.setOverlayImagePath(response.getOverlayImagePath());
        result.setAnalysisTime(response.getAnalysisTime() != null ? response.getAnalysisTime() : new Date());
        result.setTaskId(taskId);
        
        if (response.getParameters() != null) {
            result.setCellCount(response.getParameters().getCellCount());
            result.setCellArea(response.getParameters().getCellArea());
            result.setTotalArea(response.getParameters().getTotalArea());
            result.setCellRatio(response.getParameters().getCellRatio());
            result.setAvgCellSize(response.getParameters().getAvgCellSize());
        }
        
        return result;
    }
    
    /**
     * 从响应对象创建结果对象
     * 
     * @param response Fullnet分析响应
     * @return 结果对象
     */
    public static FullnetResult fromResponse(FullnetResponse response) {
        return fromResponse(response, null);
    }
} 