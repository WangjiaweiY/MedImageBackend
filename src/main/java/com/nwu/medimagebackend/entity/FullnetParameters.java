package com.nwu.medimagebackend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fullnet分析结果参数
 * <p>
 * 深度学习分析返回的各项参数
 * </p>
 * 
 * @author MedImage团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullnetParameters {
    
    /**
     * 细胞数量
     */
    @JsonProperty("cell_count")
    private Integer cellCount;
    
    /**
     * 细胞面积
     */
    @JsonProperty("cell_area")
    private Integer cellArea;
    
    /**
     * 总面积
     */
    @JsonProperty("total_area")
    private Integer totalArea;
    
    /**
     * 细胞比例(%)
     */
    @JsonProperty("cell_ratio")
    private Double cellRatio;
    
    /**
     * 平均细胞大小
     */
    @JsonProperty("avg_cell_size")
    private Double avgCellSize;
} 