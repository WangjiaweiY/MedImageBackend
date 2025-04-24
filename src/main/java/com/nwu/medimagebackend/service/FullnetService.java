package com.nwu.medimagebackend.service;

import com.nwu.medimagebackend.entity.FullnetResult;
import com.nwu.medimagebackend.entity.FullnetResponse;
import com.nwu.medimagebackend.entity.FullnetTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Fullnet深度学习分析服务接口
 * 
 * @author MedImage团队
 */
public interface FullnetService {

    /**
     * 调用Python后端进行深度学习分析
     * 
     * @param filename 要分析的文件名
     * @return 分析结果响应
     * @throws Exception 如果分析过程出错
     */
    FullnetResponse analyzeImage(String filename) throws Exception;
    
    /**
     * 异步提交分析任务
     * 
     * @param filename 要分析的文件名
     * @return 创建的任务ID
     */
    String submitAnalysisTask(String filename);
    
    /**
     * 获取任务状态
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    FullnetTask getTaskById(String taskId);
    
    /**
     * 获取文件最新的分析任务
     * 
     * @param filename 文件名
     * @return 任务对象
     */
    FullnetTask getLatestTaskByFilename(String filename);
    
    /**
     * 获取所有任务
     * 
     * @return 任务列表
     */
    List<FullnetTask> getAllTasks();
    
    /**
     * 保存分析结果到数据库
     * 
     * @param response Python后端返回的响应
     * @return 保存的结果
     */
    FullnetResult saveAnalysisResult(FullnetResponse response);
    
    /**
     * 保存分析结果到数据库，并关联任务ID
     * 
     * @param response Python后端返回的响应
     * @param taskId 异步任务ID
     * @return 保存的结果
     */
    FullnetResult saveAnalysisResult(FullnetResponse response, String taskId);
    
    /**
     * 根据ID获取分析结果
     * 
     * @param id 结果ID
     * @return 分析结果，如果未找到则返回null
     */
    FullnetResult getResultById(Long id);
    
    /**
     * 根据任务ID获取分析结果
     * 
     * @param taskId 任务ID
     * @return 分析结果，如果未找到则返回null
     */
    FullnetResult getResultByTaskId(String taskId);
    
    /**
     * 根据文件名获取分析结果
     * 
     * @param filename 文件名
     * @return 分析结果，如果未找到则返回null
     */
    FullnetResult getResultByFilename(String filename);
    
    /**
     * 获取所有分析结果
     * 
     * @return 分析结果列表
     */
    List<FullnetResult> getAllResults();

    /**
     * 获取特定文件的所有历史分析记录
     *
     * @param filename 完整文件名称（包含文件夹路径）
     * @return 历史分析记录列表
     */
    List<FullnetResult> getHistoryByFilename(String filename);

    /**
     * 模糊匹配文件名查询历史分析记录
     *
     * @param filenamePattern 文件名模式，如 "test/test1"
     * @return 历史分析记录列表
     */
    List<FullnetResult> getHistoryByFilenameLike(String filenamePattern);

    /**
     * 删除特定ID的分析结果
     *
     * @param resultId 分析结果ID
     * @return 是否删除成功
     */
    boolean deleteResult(Long resultId);
} 