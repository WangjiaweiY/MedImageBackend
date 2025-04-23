package com.nwu.medimagebackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fullnet分析请求
 * <p>
 * 用于发送给Python后端进行深度学习分析的请求体
 * </p>
 * 
 * @author MedImage团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullnetRequest {
    
    /**
     * 要分析的文件名
     */
    private String filename;
} 