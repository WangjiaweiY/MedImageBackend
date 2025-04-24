package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.entity.FullnetRequest;
import com.nwu.medimagebackend.entity.FullnetResponse;
import com.nwu.medimagebackend.entity.FullnetResult;
import com.nwu.medimagebackend.entity.FullnetTask;
import com.nwu.medimagebackend.service.FullnetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fullnet深度学习分析控制器
 * <p>
 * 处理与Fullnet深度学习分析相关的HTTP请求，包括图像分析处理和结果查询。
 * </p>
 * 
 * @author MedImage团队
 */
@RestController
@Slf4j
@RequestMapping("/api/fullnet")
public class FullnetController {

    @Autowired
    private FullnetService fullnetService;

    @Value("${uploads.fullnet.results.dir:../uploads/fullnet_results/}")
    private String fullnetResultsDir;

    /**
     * 分析图像（异步方式）
     * <p>
     * 以异步方式调用Python后端对指定文件进行深度学习分析
     * </p>
     * 
     * @param request 包含文件名的请求体
     * @return 任务ID和状态信息
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeImage(@RequestBody FullnetRequest request) {
        try {
            String filename = request.getFilename();
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.badRequest().body("文件名不能为空");
            }
            
            log.info("接收到Fullnet异步分析请求: 文件[{}]", filename);
            
            // 提交异步任务
            String taskId = fullnetService.submitAnalysisTask(filename);
            
            // 返回任务ID
            Map<String, Object> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("status", "PENDING");
            response.put("message", "任务已提交，请使用任务ID查询进度");
            
            log.info("Fullnet异步分析任务已提交: 文件[{}], 任务ID[{}]", filename, taskId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Fullnet异步分析任务提交失败: 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("任务提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务状态
     * <p>
     * 根据任务ID获取异步分析任务的状态
     * </p>
     * 
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        try {
            log.info("查询Fullnet分析任务状态: 任务ID[{}]", taskId);
            
            FullnetTask task = fullnetService.getTaskById(taskId);
            if (task == null) {
                log.warn("Fullnet分析任务不存在: 任务ID[{}]", taskId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("未找到任务ID为 " + taskId + " 的分析任务");
            }
            
            log.info("成功获取Fullnet分析任务状态: 任务ID[{}], 状态[{}]", taskId, task.getStatus());
            
            // 如果任务完成，添加结果
            if ("COMPLETED".equals(task.getStatus())) {
                FullnetResult result = null;
                
                // 首先尝试直接通过任务ID查询结果
                log.info("尝试使用任务ID直接查询分析结果: {}", taskId);
                result = fullnetService.getResultByTaskId(taskId);
                
                // 如果找不到，但任务有关联的结果ID，则使用结果ID查询
                if (result == null && task.getResultId() != null) {
                    log.info("使用结果ID查询分析结果: {}", task.getResultId());
                    result = fullnetService.getResultById(task.getResultId());
                }
                
                // 如果通过ID找不到，尝试使用文件名查询
                if (result == null && task.getFilename() != null) {
                    log.info("使用文件名查询分析结果: {}", task.getFilename());
                    result = fullnetService.getResultByFilename(task.getFilename());
                }
                
                // 如果使用文件名找不到，尝试使用actualFilename查询
                if (result == null && task.getActualFilename() != null && !task.getActualFilename().equals(task.getFilename())) {
                    log.info("使用实际文件名查询分析结果: {}", task.getActualFilename());
                    result = fullnetService.getResultByFilename(task.getActualFilename());
                }
                
                if (result != null) {
                    log.info("找到分析结果: ID[{}], 文件名[{}]", result.getId(), result.getFilename());
                    
                    // 如果结果没有关联任务ID，更新关联
                    if (result.getTaskId() == null) {
                        log.info("更新分析结果的任务ID关联: 结果ID[{}], 任务ID[{}]", result.getId(), taskId);
                        result.setTaskId(taskId);
                        fullnetService.saveAnalysisResult(null, taskId);
                    }
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("task", task);
                    response.put("result", result);
                    return ResponseEntity.ok(response);
                } else {
                    log.warn("任务已完成但找不到分析结果: 任务ID[{}], 文件名[{}], 实际文件名[{}], 结果ID[{}]", 
                             taskId, task.getFilename(), task.getActualFilename(), task.getResultId());
                }
            }
            
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            log.error("查询Fullnet分析任务状态失败: 任务ID[{}], 错误: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查询任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件的最新分析任务
     * <p>
     * 根据文件名获取最近的分析任务
     * </p>
     * 
     * @param filename 文件名
     * @return 任务状态信息
     */
    @GetMapping("/task/file")
    public ResponseEntity<?> getLatestTaskByFilename(@RequestParam("filename") String filename) {
        try {
            log.info("查询文件最新Fullnet分析任务: 文件[{}]", filename);
            
            FullnetTask task = fullnetService.getLatestTaskByFilename(filename);
            if (task == null) {
                log.warn("文件没有Fullnet分析任务: 文件[{}]", filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("未找到文件 " + filename + " 的分析任务");
            }
            
            log.info("成功获取文件最新Fullnet分析任务: 文件[{}], 任务ID[{}], 状态[{}]", 
                    filename, task.getId(), task.getStatus());
            
            // 如果任务完成，添加结果
            if ("COMPLETED".equals(task.getStatus())) {
                FullnetResult result = null;
                
                // 优先使用resultId查询结果
                if (task.getResultId() != null) {
                    log.info("使用结果ID查询分析结果: {}", task.getResultId());
                    result = fullnetService.getResultById(task.getResultId());
                }
                
                // 如果通过ID找不到，尝试使用文件名查询
                if (result == null && task.getFilename() != null) {
                    log.info("使用文件名查询分析结果: {}", task.getFilename());
                    result = fullnetService.getResultByFilename(task.getFilename());
                }
                
                // 如果使用actualFilename找不到，尝试使用actualFilename查询
                if (result == null && task.getActualFilename() != null && !task.getActualFilename().equals(task.getFilename())) {
                    log.info("使用实际文件名查询分析结果: {}", task.getActualFilename());
                    result = fullnetService.getResultByFilename(task.getActualFilename());
                }
                
                if (result != null) {
                    log.info("找到分析结果: ID[{}], 文件名[{}]", result.getId(), result.getFilename());
                    Map<String, Object> response = new HashMap<>();
                    response.put("task", task);
                    response.put("result", result);
                    return ResponseEntity.ok(response);
                } else {
                    log.warn("任务已完成但找不到分析结果: 任务ID[{}], 文件名[{}], 实际文件名[{}], 结果ID[{}]", 
                             task.getId(), task.getFilename(), task.getActualFilename(), task.getResultId());
                }
            }
            
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            log.error("查询文件最新Fullnet分析任务失败: 文件[{}], 错误: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查询任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有分析任务
     * <p>
     * 获取所有异步分析任务的状态
     * </p>
     * 
     * @return 任务列表
     */
    @GetMapping("/tasks")
    public ResponseEntity<?> getAllTasks() {
        try {
            log.info("查询所有Fullnet分析任务");
            
            List<FullnetTask> tasks = fullnetService.getAllTasks();
            if (tasks == null || tasks.isEmpty()) {
                log.warn("无Fullnet分析任务");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("未找到任何分析任务");
            }
            
            log.info("成功获取所有Fullnet分析任务: 共{}个任务", tasks.size());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("查询所有Fullnet分析任务失败: 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查询任务列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分析结果
     * <p>
     * 根据文件名获取已保存的分析结果
     * </p>
     * 
     * @param filename 文件名
     * @return 分析结果
     */
    @GetMapping("/result")
    public ResponseEntity<?> getAnalysisResult(@RequestParam("filename") String filename) {
        try {
            log.info("查询Fullnet分析结果: 文件[{}]", filename);
            
            FullnetResult result = fullnetService.getResultByFilename(filename);
            if (result == null) {
                log.warn("Fullnet分析结果不存在: 文件[{}]", filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("未找到文件 " + filename + " 的分析结果");
            }
            
            log.info("成功获取Fullnet分析结果: 文件[{}]", filename);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询Fullnet分析结果失败: 文件[{}], 错误: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查询分析结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有分析结果
     * <p>
     * 获取所有已保存的分析结果
     * </p>
     * 
     * @return 分析结果列表
     */
    @GetMapping("/results")
    public ResponseEntity<?> getAllResults() {
        try {
            log.info("查询所有Fullnet分析结果");
            
            List<FullnetResult> results = fullnetService.getAllResults();
            if (results == null || results.isEmpty()) {
                log.warn("无Fullnet分析结果");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("未找到任何分析结果");
            }
            
            log.info("成功获取所有Fullnet分析结果: 共{}个结果", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("查询所有Fullnet分析结果失败: 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查询分析结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务关联的分析结果
     * <p>
     * 根据任务ID获取关联的分析结果
     * </p>
     * 
     * @param taskId 任务ID
     * @return 分析结果
     */
    @GetMapping("/task/{taskId}/result")
    public ResponseEntity<?> getTaskResult(@PathVariable String taskId) {
        try {
            log.info("查询任务关联的分析结果: 任务ID[{}]", taskId);
            
            FullnetTask task = fullnetService.getTaskById(taskId);
            if (task == null) {
                log.warn("Fullnet分析任务不存在: 任务ID[{}]", taskId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("未找到任务ID为 " + taskId + " 的分析任务");
            }
            
            if (!"COMPLETED".equals(task.getStatus())) {
                log.warn("任务尚未完成: 任务ID[{}], 当前状态[{}]", taskId, task.getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("任务尚未完成，无法获取结果: 当前状态=" + task.getStatus());
            }
            
            // 首先尝试直接通过任务ID查询结果
            FullnetResult result = fullnetService.getResultByTaskId(taskId);
            
            // 如果找不到，尝试其他查询方式
            if (result == null) {
                // 多种方式尝试获取结果
                if (task.getResultId() != null) {
                    result = fullnetService.getResultById(task.getResultId());
                }
                
                if (result == null && task.getFilename() != null) {
                    result = fullnetService.getResultByFilename(task.getFilename());
                }
                
                if (result == null && task.getActualFilename() != null) {
                    result = fullnetService.getResultByFilename(task.getActualFilename());
                }
                
                // 如果找到结果，更新关联
                if (result != null && result.getTaskId() == null) {
                    log.info("更新分析结果的任务ID关联: 结果ID[{}], 任务ID[{}]", result.getId(), taskId);
                    result.setTaskId(taskId);
                    fullnetService.saveAnalysisResult(null, taskId);
                }
            }
            
            if (result == null) {
                log.warn("找不到任务关联的分析结果: 任务ID[{}]", taskId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("找不到任务ID为 " + taskId + " 的分析结果");
            }
            
            log.info("成功获取任务关联的分析结果: 任务ID[{}], 结果ID[{}]", taskId, result.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询任务关联的分析结果失败: 任务ID[{}], 错误: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查询分析结果失败: " + e.getMessage());
        }
    }

    /**
     * 获取Fullnet分析结果图像
     * 
     * @param request HTTP请求
     * @return 图像资源
     */
    @GetMapping("/images/**")
    public ResponseEntity<Resource> getFullnetImage(HttpServletRequest request) {
        try {
            // 获取完整的请求路径
            String fullPath = request.getRequestURI();
            log.info("请求Fullnet图像: {}", fullPath);
            
            // 提取 /api/fullnet/images/ 之后的部分作为相对路径
            String relativePath = fullPath.substring("/api/fullnet/images/".length());
            log.info("提取的相对路径: {}", relativePath);
            
            // 如果路径包含fullnet_results前缀，移除它
            if (relativePath.startsWith("fullnet_results/")) {
                relativePath = relativePath.substring("fullnet_results/".length());
                log.info("处理后的路径: {}", relativePath);
            }
            
            // 构建完整的文件路径
            Path filePath = Paths.get(fullnetResultsDir).resolve(relativePath).normalize();
            log.info("尝试访问文件: {}", filePath);
            
            // 安全检查，确保文件路径在允许的目录内
            if (!filePath.startsWith(Paths.get(fullnetResultsDir).normalize())) {
                log.warn("非法图像访问请求: {}", relativePath);
                return ResponseEntity.badRequest().build();
            }
            
            // 读取文件
            Resource resource = new FileSystemResource(filePath);
            
            // 检查文件是否存在
            if (resource.exists() && resource.isReadable()) {
                // 根据文件扩展名设置Content-Type
                String contentType = determineContentType(filePath);
                
                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                
                log.info("成功返回图像: {}", filePath);
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                log.warn("图像不存在或无法读取: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取图像失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 确定文件的Content-Type
     */
    private String determineContentType(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".tif") || fileName.endsWith(".tiff")) {
            return "image/tiff";
        } else if (fileName.endsWith(".svs")) {
            return "image/svs";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * 获取特定文件的所有历史Fullnet分析记录
     *
     * @param folderName 文件夹名称
     * @param fileName 文件名称
     * @return 历史分析记录列表
     */
    @GetMapping("/history")
    public ResponseEntity<List<FullnetResult>> getHistory(
            @RequestParam String folderName,
            @RequestParam String fileName) {
        log.info("获取历史分析记录: folderName={}, fileName={}", folderName, fileName);
        try {
            // 构建完整文件名
            String fullFilename = folderName + "/" + fileName;
            
            // 如果fileName已经包含文件扩展名，则直接使用
            // 否则尝试多种常见的文件扩展名
            List<FullnetResult> history = new ArrayList<>();
            if (fileName.contains(".")) {
                // 已经包含扩展名
                history = fullnetService.getHistoryByFilename(fullFilename);
            } else {
                // 尝试常见图像格式后缀
                for (String ext : new String[]{"", ".png", ".jpg", ".jpeg", ".tif", ".tiff", ".svs"}) {
                    List<FullnetResult> results = fullnetService.getHistoryByFilename(fullFilename + ext);
                    if (results != null && !results.isEmpty()) {
                        history = results;
                        log.info("找到文件历史记录(后缀{}): {}", ext, fullFilename + ext);
                        break;
                    }
                }
                
                // 如果没有找到结果，使用模糊匹配
                if (history.isEmpty()) {
                    log.info("使用模糊匹配查询历史记录: {}", fullFilename);
                    history = fullnetService.getHistoryByFilenameLike(fullFilename);
                    log.info("模糊匹配找到记录数: {}", history.size());
                }
            }
            
            log.info("找到历史记录总数: {}", history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取历史分析记录失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据结果ID获取单个分析结果的详细信息
     *
     * @param resultId 分析结果ID
     * @return 分析结果详情
     */
    @GetMapping("/result/{resultId}")
    public ResponseEntity<FullnetResult> getResult(@PathVariable Long resultId) {
        log.info("获取分析结果详情: resultId={}", resultId);
        try {
            FullnetResult result = fullnetService.getResultById(resultId);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取分析结果详情失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除特定ID的历史分析结果
     *
     * @param resultId 分析结果ID
     * @return 删除操作结果
     */
    @DeleteMapping("/result/{resultId}")
    public ResponseEntity<Map<String, Object>> deleteResult(@PathVariable Long resultId) {
        log.info("删除分析结果: resultId={}", resultId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = fullnetService.deleteResult(resultId);
            if (deleted) {
                response.put("success", true);
                response.put("message", "分析结果已成功删除");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "未找到指定的分析结果");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("删除分析结果失败: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "删除分析结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 