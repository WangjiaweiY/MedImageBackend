package com.nwu.medimagebackend.service.impl;

import com.nwu.medimagebackend.entity.FileInfo;
import com.nwu.medimagebackend.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
public class RegistrationServiceimpl implements RegistrationService {

    // 从配置文件中读取上传目录，默认为相对路径 "./uploads/svs/"
    @Value("${uploads.svs.dir:./uploads/svs/}")
    private String svsUploadDir;

    @Override
    public Map<String, Object> handleSvsUpload(MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("未选择任何文件");
        }

        // 将相对路径转换为绝对路径，并创建目录（如果不存在）
        Path baseDir = Paths.get(svsUploadDir).toAbsolutePath().normalize();
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }

        // 从第一个文件中提取文件夹名称（假设格式为 folderName/xxx...）
        String folderName = "";
        String firstFilePath = files[0].getOriginalFilename();
        if (StringUtils.hasText(firstFilePath) && firstFilePath.contains("/")) {
            folderName = firstFilePath.substring(0, firstFilePath.indexOf("/"));
        }

        // 遍历所有上传的文件，按照相对路径保存
        for (MultipartFile file : files) {
            String relativePath = file.getOriginalFilename();
            if (!StringUtils.hasText(relativePath)) {
                continue;
            }
            // 构造目标路径：baseDir + relativePath
            Path targetPath = baseDir.resolve(relativePath).normalize();
            // 确保目标父目录存在
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
            log.info("保存文件至: {}", targetPath.toString());
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "上传完毕");
        responseMap.put("folder", folderName);
        return responseMap;
    }

    @Override
    public List<FileInfo> listSvsFiles() {
        List<FileInfo> result = new ArrayList<>();
        File baseDir = new File(svsUploadDir);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return result;
        }
        // 遍历 svs 上传目录下的每个子文件夹
        File[] subDirs = baseDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                result.add(new FileInfo(subDir.getName()));
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> registerFolder(String folderName) {
        log.info("接收到文件夹 [{}] 的配准请求", folderName);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8000/register";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("folder", folderName);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("配准服务返回错误状态: {}", response.getStatusCode());
            throw new RuntimeException("配准失败");
        }

        log.info("配准结果：{}", response.getBody());
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "配准完毕");
        responseMap.put("folder", folderName);
        return responseMap;
    }
}
