package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.entity.FileInfo;
import com.nwu.medimagebackend.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 配准控制器
 * <p>
 * 处理与医学图像配准相关的HTTP请求，包括图像上传、文件列表查询和配准处理等操作。
 * 配准是将不同时间或不同模态获取的医学图像对齐的过程，有助于医疗诊断和分析。
 * </p>
 * 
 * @author MedImage团队
 */
@RestController
@RequestMapping("/api/svs")
@Slf4j
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 上传配准图像文件接口
     * <p>
     * 接收上传的医学图像文件并保存到服务器，用于后续配准处理。
     * </p>
     * 
     * @param files 上传的文件数组
     * @return 包含上传结果的响应
     */
    @PostMapping("/upload")
    public ResponseEntity<?> handleSvsUpload(@RequestParam("files") MultipartFile[] files) {
        try {
            log.info("接收到{}个文件的上传请求", files.length);
            for (MultipartFile file : files) {
                log.debug("接收到文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            }
            
            Map<String, Object> response = registrationService.handleSvsUpload(files);
            log.info("文件上传处理成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件上传处理失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "上传失败：" + e.getMessage()));
        }
    }

    /**
     * 列出上传目录下的文件夹信息
     * <p>
     * 获取所有已上传的医学图像文件夹及其内容信息。
     * </p>
     * 
     * @return 包含文件夹信息的响应
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listSvsFiles() {
        log.info("请求获取配准文件列表");
        List<FileInfo> result = registrationService.listSvsFiles();
        log.info("成功返回配准文件列表，包含{}个文件夹", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 调用外部配准服务处理指定文件夹的配准操作
     * <p>
     * 对指定文件夹中的医学图像执行配准处理，通过外部服务完成图像对齐。
     * </p>
     * 
     * @param folderName 要处理的文件夹名称
     * @param requestBody 请求体，包含用户名等信息
     * @return 包含配准结果的响应
     */
    @PostMapping("/register/{folder}")
    public ResponseEntity<Map<String, Object>> registerFolder(
            @PathVariable("folder") String folderName,
            @RequestBody Map<String, String> requestBody) {
        try {
            String userName = requestBody.get("username");
            log.info("接收到文件夹[{}]的配准请求，用户: {}", folderName, userName);
            
            Map<String, Object> response = registrationService.registerFolder(folderName, userName);
            log.info("文件夹[{}]配准处理成功", folderName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件夹[{}]配准处理失败: {}", folderName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "配准失败：" + e.getMessage()));
        }
    }
}
