package com.nwu.medimagebackend.controller;

import com.nwu.medimagebackend.common.FileInfo;
import com.nwu.medimagebackend.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/svs")
@CrossOrigin(origins = "*")
@Slf4j
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 上传配准图像文件夹接口
     */
    @PostMapping("/upload")
    public ResponseEntity<?> handleSvsUpload(@RequestParam("files") MultipartFile[] files) {
        try {
            log.info("上传文件：" + files);
            Map<String, Object> response = registrationService.handleSvsUpload(files);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "上传失败：" + e.getMessage()));
        }
    }

    /**
     * 列出上传目录下的文件夹信息
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listSvsFiles() {
        log.info("拉取配准文件列表");
        List<FileInfo> result = registrationService.listSvsFiles();
        return ResponseEntity.ok(result);
    }

    /**
     * 调用外部配准服务处理指定文件夹的配准操作
     */
    @PostMapping("/register/{folder}")
    public ResponseEntity<Map<String, Object>> registerFolder(@PathVariable("folder") String folderName) {
        try {
            Map<String, Object> response = registrationService.registerFolder(folderName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("配准请求异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "配准失败：" + e.getMessage()));
        }
    }
}
