package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.entity.FullnetParameters;
import com.nwu.medimagebackend.entity.FullnetRequest;
import com.nwu.medimagebackend.entity.FullnetResponse;
import com.nwu.medimagebackend.entity.FullnetResult;
import com.nwu.medimagebackend.entity.FullnetTask;
import com.nwu.medimagebackend.mapper.FullnetMapper;
import com.nwu.medimagebackend.mapper.FullnetTaskMapper;
import com.nwu.medimagebackend.service.FullnetService;
import com.nwu.medimagebackend.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Fullnet深度学习分析服务实现类
 * 
 * @author MedImage团队
 */
@Service
@Slf4j
public class FullnetServiceImpl implements FullnetService {

    @Autowired
    private FullnetMapper fullnetMapper;
    
    @Autowired
    private FullnetTaskMapper taskMapper;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Python后端API地址
     */
    @Value("${python.backend.url:http://localhost:8000}")
    private String pythonBackendUrl;
    
    /**
     * 输入图像文件目录
     */
    @Value("${uploads.svs.dir:../uploads/svs/}")
    private String svsDir;
    
    /**
     * 分析结果输出目录
     */
    @Value("${uploads.fullnet.results.dir:../uploads/fullnet_results/}")
    private String fullnetResultsDir;
    
    /**
     * 服务初始化
     */
    @PostConstruct
    public void init() {
        log.info("初始化Fullnet分析服务");
        log.info("Python后端URL: {}", pythonBackendUrl);
        log.info("SVS图像目录: {}", svsDir);
        log.info("Fullnet结果目录: {}", fullnetResultsDir);
        
        // 确保目录存在
        PathUtils.ensureDirectoryExists(svsDir);
        PathUtils.ensureDirectoryExists(fullnetResultsDir);
        
        // 初始化RestTemplate（如果没有自动装配）
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }
    }
    
    /**
     * 调用Python后端进行深度学习分析
     * 
     * @param filename 要分析的文件名
     * @return 分析结果响应
     * @throws Exception 如果分析过程出错
     */
    @Override
    public FullnetResponse analyzeImage(String filename) throws Exception {
        log.info("调用Python后端分析图像: {}", filename);
        
        // 首先检查是否已有分析结果
        FullnetResult existingResult = fullnetMapper.findByFilename(filename);
        if (existingResult != null) {
            log.info("找到已有分析结果，无需重新分析: {}", filename);
            FullnetResponse response = new FullnetResponse();
            response.setFilename(existingResult.getFilename());
            response.setResultImagePath(existingResult.getResultImagePath());
            response.setOverlayImagePath(existingResult.getOverlayImagePath());
            response.setAnalysisTime(existingResult.getAnalysisTime());
            response.setStatus("success");
            
            // 设置参数
            FullnetParameters parameters = new FullnetParameters();
            parameters.setCellCount(existingResult.getCellCount());
            parameters.setCellArea(existingResult.getCellArea());
            parameters.setTotalArea(existingResult.getTotalArea());
            parameters.setCellRatio(existingResult.getCellRatio());
            parameters.setAvgCellSize(existingResult.getAvgCellSize());
            response.setParameters(parameters);
            
            return response;
        }
        
        // 由于文件可能有不同后缀，我们尝试多种可能的格式
        File inputFile = null;
        boolean fileExists = false;
        String actualFilename = filename; // 保存带后缀的实际文件名
        
        // 检查不带后缀名的情况
        Path inputBasePath = Paths.get(svsDir, filename);
        inputFile = inputBasePath.toFile();
        if (inputFile.exists() && inputFile.isFile()) {
            fileExists = true;
            actualFilename = filename; // 无后缀
            log.info("找到文件（无后缀）: {}", inputBasePath);
        }
        
        // 尝试常见图像格式后缀
        String[] extensions = {".svs", ".png", ".jpg", ".jpeg", ".tif", ".tiff"};
        for (String ext : extensions) {
            Path extPath = Paths.get(svsDir, filename + ext);
            File extFile = extPath.toFile();
            if (extFile.exists() && extFile.isFile()) {
                inputFile = extFile;
                fileExists = true;
                actualFilename = filename + ext; // 添加后缀
                log.info("找到文件（后缀{}）: {}", ext, extPath);
                break;
            }
        }
        
        if (!fileExists) {
            // 尝试在目录中查找以filename开头的文件
            File dir = new File(svsDir);
            File[] matchingFiles = dir.listFiles((d, name) -> name.startsWith(filename + ".") || name.equals(filename));
            
            if (matchingFiles != null && matchingFiles.length > 0) {
                inputFile = matchingFiles[0];
                fileExists = true;
                actualFilename = inputFile.getName(); // 使用完整文件名
                log.info("找到匹配文件: {}", inputFile.getAbsolutePath());
            }
        }
        
        if (!fileExists) {
            log.error("输入文件不存在，已尝试多种后缀: {}", inputBasePath);
            throw new Exception("输入文件不存在: " + filename + "（已尝试常见图像格式后缀）");
        }
        
        // 构建请求
        String url = pythonBackendUrl + "/fullnet";
        // 使用带后缀的实际文件名
        FullnetRequest request = new FullnetRequest(actualFilename);
        
        log.info("向Python后端发送请求，文件名: {}", actualFilename);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FullnetRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            // 发送请求并获取响应
            log.info("发送请求到Python后端，可能需要较长时间处理...");
            FullnetResponse response = restTemplate.postForObject(url, entity, FullnetResponse.class);
            
            if (response == null || !"success".equals(response.getStatus())) {
                String errorMsg = response != null ? response.getMessage() : "未知错误";
                log.error("Python后端分析失败: {}", errorMsg);
                throw new Exception("Python后端分析失败: " + errorMsg);
            }
            
            // 设置分析时间
            response.setAnalysisTime(new Date());
            
            // 记录返回的分析结果路径
            log.info("Python分析完成，结果图像路径: {}, 叠加图像路径: {}", 
                    response.getResultImagePath(), response.getOverlayImagePath());
            
            // 检查返回的参数
            if (response.getParameters() != null) {
                log.info("分析参数: 细胞数量={}, 细胞面积={}, 总面积={}, 细胞比例={}, 平均细胞大小={}", 
                        response.getParameters().getCellCount(),
                        response.getParameters().getCellArea(),
                        response.getParameters().getTotalArea(),
                        response.getParameters().getCellRatio(),
                        response.getParameters().getAvgCellSize());
            } else {
                log.warn("Python分析返回的参数为空");
            }
            
            // 保存分析结果
            FullnetResult result = saveAnalysisResult(response);
            if (result == null) {
                log.error("保存分析结果失败");
            }
            
            log.info("Python后端分析成功: {}", actualFilename);
            return response;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 处理超时异常
            if (e.getMessage().contains("Read timed out")) {
                log.error("调用Python后端分析超时，图像处理可能需要更长时间: {}", e.getMessage(), e);
                throw new Exception("图像分析处理超时，这可能是因为图像较大或复杂。请稍后使用查询接口检查分析结果，或尝试使用更小的图像。");
            } else {
                log.error("调用Python后端分析时网络错误: {}", e.getMessage(), e);
                throw new Exception("调用Python后端分析时网络错误: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("调用Python后端分析时出错: {}", e.getMessage(), e);
            throw new Exception("调用Python后端分析时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 异步提交分析任务
     * 
     * @param filename 要分析的文件名
     * @return 创建的任务ID
     */
    @Override
    public String submitAnalysisTask(String filename) {
        log.info("异步提交分析任务: {}", filename);
        
        // 生成任务ID
        String taskId = UUID.randomUUID().toString();
        
        // 创建任务记录
        FullnetTask task = new FullnetTask();
        task.setId(taskId);
        task.setFilename(filename);
        task.setStatus("PENDING");
        task.setProgress("等待处理...");
        task.setCreatedTime(new Date());
        
        // 保存任务
        taskMapper.insertTask(task);
        
        // 异步执行分析
        processAnalysisTaskAsync(taskId, filename);
        
        return taskId;
    }
    
    /**
     * 异步处理分析任务
     * 
     * @param taskId 任务ID
     * @param filename 文件名
     */
    @Async("taskExecutor")
    protected void processAnalysisTaskAsync(String taskId, String filename) {
        log.info("开始处理异步分析任务 [{}] 文件: {}", taskId, filename);
        
        FullnetTask task = taskMapper.findById(taskId);
        if (task == null) {
            log.error("任务 [{}] 不存在", taskId);
            return;
        }
        
        try {
            // 更新任务状态为处理中
            task.setStatus("PROCESSING");
            task.setProgress("正在查找文件...");
            taskMapper.updateTask(task);
            
            // 由于文件可能有不同后缀，我们尝试多种可能的格式
            File inputFile = null;
            boolean fileExists = false;
            String actualFilename = filename; // 保存带后缀的实际文件名
            
            // 检查不带后缀名的情况
            Path inputBasePath = Paths.get(svsDir, filename);
            inputFile = inputBasePath.toFile();
            if (inputFile.exists() && inputFile.isFile()) {
                fileExists = true;
                actualFilename = filename; // 无后缀
                log.info("找到文件（无后缀）: {}", inputBasePath);
            }
            
            // 尝试常见图像格式后缀
            String[] extensions = {".svs", ".png", ".jpg", ".jpeg", ".tif", ".tiff"};
            for (String ext : extensions) {
                Path extPath = Paths.get(svsDir, filename + ext);
                File extFile = extPath.toFile();
                if (extFile.exists() && extFile.isFile()) {
                    inputFile = extFile;
                    fileExists = true;
                    actualFilename = filename + ext; // 添加后缀
                    log.info("找到文件（后缀{}）: {}", ext, extPath);
                    break;
                }
            }
            
            if (!fileExists) {
                // 尝试在目录中查找以filename开头的文件
                File dir = new File(svsDir);
                File[] matchingFiles = dir.listFiles((d, name) -> name.startsWith(filename + ".") || name.equals(filename));
                
                if (matchingFiles != null && matchingFiles.length > 0) {
                    inputFile = matchingFiles[0];
                    fileExists = true;
                    actualFilename = inputFile.getName(); // 使用完整文件名
                    log.info("找到匹配文件: {}", inputFile.getAbsolutePath());
                }
            }
            
            if (!fileExists) {
                log.error("输入文件不存在，已尝试多种后缀: {}", inputBasePath);
                task.setStatus("FAILED");
                task.setProgress("");
                task.setErrorMessage("输入文件不存在，已尝试常见图像格式后缀");
                task.setCompletedTime(new Date());
                taskMapper.updateTask(task);
                return;
            }
            
            // 更新任务的实际文件名
            task.setActualFilename(actualFilename);
            task.setProgress("正在调用Python后端分析...");
            taskMapper.updateTask(task);
            
            // 构建请求
            String url = pythonBackendUrl + "/fullnet";
            // 使用带后缀的实际文件名
            FullnetRequest request = new FullnetRequest(actualFilename);
            
            log.info("向Python后端发送异步请求，任务 [{}]，文件名: {}", taskId, actualFilename);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FullnetRequest> entity = new HttpEntity<>(request, headers);
            
            // 发送请求并获取响应
            log.info("发送请求到Python后端，可能需要较长时间处理...");
            FullnetResponse response = restTemplate.postForObject(url, entity, FullnetResponse.class);
            
            if (response == null || !"success".equals(response.getStatus())) {
                String errorMsg = response != null ? response.getMessage() : "未知错误";
                log.error("Python后端分析失败: {}", errorMsg);
                
                task.setStatus("FAILED");
                task.setProgress("");
                task.setErrorMessage("Python后端分析失败: " + errorMsg);
                task.setCompletedTime(new Date());
                taskMapper.updateTask(task);
                return;
            }
            
            // 设置分析时间
            response.setAnalysisTime(new Date());
            
            // 记录返回的分析结果路径
            log.info("Python分析完成，结果图像路径: {}, 叠加图像路径: {}", 
                    response.getResultImagePath(), response.getOverlayImagePath());
            
            // 检查返回的参数
            if (response.getParameters() != null) {
                log.info("分析参数: 细胞数量={}, 细胞面积={}, 总面积={}, 细胞比例={}, 平均细胞大小={}", 
                        response.getParameters().getCellCount(),
                        response.getParameters().getCellArea(),
                        response.getParameters().getTotalArea(),
                        response.getParameters().getCellRatio(),
                        response.getParameters().getAvgCellSize());
            } else {
                log.warn("Python分析返回的参数为空");
            }
            
            // 更新任务状态
            task.setProgress("正在保存分析结果...");
            taskMapper.updateTask(task);
            
            // 保存分析结果
            FullnetResult result = saveAnalysisResult(response, taskId);
            
            if (result == null) {
                log.error("保存分析结果失败");
                task.setStatus("FAILED");
                task.setProgress("");
                task.setErrorMessage("保存分析结果失败");
                task.setCompletedTime(new Date());
                taskMapper.updateTask(task);
                return;
            }
            
            // 更新任务状态为完成
            task.setStatus("COMPLETED");
            task.setProgress("分析完成");
            task.setCompletedTime(new Date());
            task.setResultId(result.getId());
            taskMapper.updateTask(task);
            
            log.info("异步任务 [{}] 完成，Python后端分析成功: {}", taskId, actualFilename);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 处理超时异常
            String errorMsg;
            if (e.getMessage().contains("Read timed out")) {
                errorMsg = "调用Python后端分析超时，可能是因为图像较大或复杂";
                log.error("异步任务 [{}] 失败: {}", taskId, errorMsg, e);
            } else {
                errorMsg = "调用Python后端分析时网络错误: " + e.getMessage();
                log.error("异步任务 [{}] 失败: {}", taskId, errorMsg, e);
            }
            
            task.setStatus("FAILED");
            task.setProgress("");
            task.setErrorMessage(errorMsg);
            task.setCompletedTime(new Date());
            taskMapper.updateTask(task);
        } catch (Exception e) {
            log.error("异步任务 [{}] 处理出错: {}", taskId, e.getMessage(), e);
            
            task.setStatus("FAILED");
            task.setProgress("");
            task.setErrorMessage("处理出错: " + e.getMessage());
            task.setCompletedTime(new Date());
            taskMapper.updateTask(task);
        }
    }
    
    /**
     * 获取任务状态
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    @Override
    public FullnetTask getTaskById(String taskId) {
        return taskMapper.findById(taskId);
    }
    
    /**
     * 获取文件最新的分析任务
     * 
     * @param filename 文件名
     * @return 任务对象
     */
    @Override
    public FullnetTask getLatestTaskByFilename(String filename) {
        return taskMapper.findLatestByFilename(filename);
    }
    
    /**
     * 获取所有任务
     * 
     * @return 任务列表
     */
    @Override
    public List<FullnetTask> getAllTasks() {
        return taskMapper.findAll();
    }
    
    /**
     * 保存分析结果到数据库
     * 
     * @param response Python后端返回的响应
     * @return 保存的结果
     */
    @Override
    public FullnetResult saveAnalysisResult(FullnetResponse response) {
        return saveAnalysisResult(response, null);
    }
    
    /**
     * 保存分析结果到数据库，并关联任务ID
     * 
     * @param response Python后端返回的响应
     * @param taskId 异步任务ID
     * @return 保存的结果
     */
    @Override
    public FullnetResult saveAnalysisResult(FullnetResponse response, String taskId) {
        // 处理仅更新任务ID的情况
        if (response == null && taskId != null) {
            log.info("使用任务ID更新已有分析结果: {}", taskId);
            try {
                // 查找已有结果
                FullnetResult existingResult = fullnetMapper.findByTaskId(taskId);
                if (existingResult != null) {
                    log.info("找到已有任务关联结果，更新任务ID: {}", existingResult.getId());
                    return existingResult;
                }
                
                log.warn("无法找到任务ID关联的分析结果，无法更新: {}", taskId);
                return null;
            } catch (Exception e) {
                log.error("更新分析结果的任务ID时出错: {}", e.getMessage(), e);
                return null;
            }
        }
        
        // 正常处理response不为null的情况
        log.info("保存Fullnet分析结果: {}", response.getFilename());
        
        // 将响应转换为结果对象
        FullnetResult result = FullnetResult.fromResponse(response, taskId);
        
        // 检查是否已存在
        FullnetResult existingResult = fullnetMapper.findByFilename(response.getFilename());
        
        try {
            if (existingResult == null) {
                // 插入新记录
                fullnetMapper.insertFullnetResult(result);
                log.info("成功插入Fullnet分析结果: {} (ID: {})", response.getFilename(), result.getId());
            } else {
                // 更新现有记录
                result.setId(existingResult.getId());
                fullnetMapper.updateFullnetResult(result);
                log.info("成功更新Fullnet分析结果: {} (ID: {})", response.getFilename(), result.getId());
            }
            
            // 确保有结果ID
            if (result.getId() == null) {
                log.warn("保存后的结果ID为null，尝试再次查询获取ID");
                FullnetResult savedResult = fullnetMapper.findByFilename(response.getFilename());
                if (savedResult != null) {
                    result.setId(savedResult.getId());
                    log.info("获取到保存的结果ID: {}", result.getId());
                } else {
                    log.error("无法获取保存的结果ID");
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("保存Fullnet分析结果时出错: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 根据文件名获取分析结果
     * 
     * @param filename 文件名
     * @return 分析结果，如果未找到则返回null
     */
    @Override
    public FullnetResult getResultByFilename(String filename) {
        return fullnetMapper.findByFilename(filename);
    }
    
    /**
     * 获取所有分析结果
     * 
     * @return 分析结果列表
     */
    @Override
    public List<FullnetResult> getAllResults() {
        return fullnetMapper.findAll();
    }
    
    /**
     * 获取特定文件的所有历史分析记录
     *
     * @param filename 完整文件名称（包含文件夹路径）
     * @return 历史分析记录列表
     */
    @Override
    public List<FullnetResult> getHistoryByFilename(String filename) {
        log.info("获取文件的历史分析记录: {}", filename);
        return fullnetMapper.findAllByFilename(filename);
    }
    
    /**
     * 根据ID获取分析结果
     *
     * @param resultId 分析结果ID
     * @return 分析结果对象
     */
    @Override
    public FullnetResult getResultById(Long resultId) {
        log.info("根据ID获取分析结果: {}", resultId);
        if (resultId == null) {
            log.warn("查询结果的ID为null");
            return null;
        }
        return fullnetMapper.findById(resultId);
    }
    
    /**
     * 根据任务ID获取分析结果
     * 
     * @param taskId 任务ID
     * @return 分析结果，如果未找到则返回null
     */
    @Override
    public FullnetResult getResultByTaskId(String taskId) {
        if (taskId == null) {
            log.warn("查询结果的任务ID为null");
            return null;
        }
        return fullnetMapper.findByTaskId(taskId);
    }
    
    /**
     * 删除特定ID的分析结果
     *
     * @param resultId 分析结果ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteResult(Long resultId) {
        log.info("删除分析结果: {}", resultId);
        if (resultId == null) {
            log.warn("删除结果的ID为null");
            return false;
        }
        
        try {
            int rowsAffected = fullnetMapper.deleteById(resultId);
            boolean success = rowsAffected > 0;
            if (success) {
                log.info("成功删除分析结果: {}", resultId);
            } else {
                log.warn("删除分析结果失败，可能不存在ID: {}", resultId);
            }
            return success;
        } catch (Exception e) {
            log.error("删除分析结果时出错: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 模糊匹配文件名查询历史分析记录
     *
     * @param filenamePattern 文件名模式，如 "test/test1"
     * @return 历史分析记录列表
     */
    @Override
    public List<FullnetResult> getHistoryByFilenameLike(String filenamePattern) {
        log.info("模糊匹配文件名查询历史记录: {}", filenamePattern);
        return fullnetMapper.findAllByFilenameLike(filenamePattern);
    }
} 