package com.nwu.medimagebackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/svs")
@CrossOrigin(origins = "*")
@Slf4j
public class RegistrationController {

    // 从配置文件中读取上传目录，默认为相对路径 "./uploads/svs/"
    @Value("${uploads.svs.dir:./uploads/svs/}")
    private String svsUploadDir;

    /**
     * 上传配准图像文件夹接口
     * 前端通过 <input type="file" webkitdirectory multiple> 上传整个文件夹时，
     * 每个文件的 getOriginalFilename() 包含其相对路径（webkitRelativePath）。
     * 后端将根据该相对路径，在 uploads/svs/ 下还原目录结构保存文件。
     */
    @PostMapping("/upload")
    public ResponseEntity<?> handleSvsUpload(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("{\"error\": \"未选择任何文件\"}");
        }
        try {
            // 将相对路径转换为绝对路径
            Path baseDir = Paths.get(svsUploadDir).toAbsolutePath().normalize();
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            // 遍历所有上传的文件，按照相对路径保存
            for (MultipartFile file : files) {
                String relativePath = file.getOriginalFilename();
                if (relativePath == null || relativePath.trim().isEmpty()) {
                    continue;
                }
                // 构造目标路径：baseDir + relativePath
                Path targetPath = baseDir.resolve(relativePath).normalize();
                // 确保目标父目录存在
                Files.createDirectories(targetPath.getParent());
                file.transferTo(targetPath.toFile());
                log.info("保存文件至: " + targetPath.toString());
            }
            return ResponseEntity.ok("{\"message\": \"上传成功\"}");
        } catch (IOException e) {
            log.error("上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"上传失败\"}");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<DziListController.FileInfo>> listDziFiles() {
        List<DziListController.FileInfo> result = new ArrayList<>();
        File baseDir = new File(svsUploadDir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return ResponseEntity.ok(result);
        }
        // 遍历 uploads/dzi 下的每个子文件夹
        File[] subDirs = baseDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                List<String> fileNames = new ArrayList<>();
                File[] files = subDir.listFiles(File::isFile);
                result.add(new DziListController.FileInfo(subDir.getName()));
            }
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register/{folder}")
    public ResponseEntity<Map<String, Object>> registerFolder(@PathVariable("folder") String folderName) {
        // 打印日志，记录接收到的文件夹名称
        log.info("接收到文件夹 [" + folderName + "] 的配准请求");

        // TODO: 在此处添加具体的图像配准逻辑

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8000/register";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("folder", folderName);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
        System.out.println("配准结果：" + response.getBody());
        // 构造返回的 JSON 响应
        Map<String, Object> response1 = new HashMap<>();
        response1.put("message", "配准任务已启动");
        response1.put("folder", folderName);
        return ResponseEntity.ok(response1);
    }
}
