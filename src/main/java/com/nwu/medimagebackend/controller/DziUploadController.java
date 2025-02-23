package com.nwu.medimagebackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/dzi")
@CrossOrigin(origins = "*")
@Slf4j
public class DziUploadController {

    // 从配置文件中读取上传目录，默认为相对路径 "./uploads/dzi/"
    @Value("${uploads.dzi.dir:./uploads/dzi/}")
    private String dziUploadDir;

    /**
     * 上传 ZIP 文件并解压
     */
    @PostMapping("/upload")
    public ResponseEntity<?> handleDziUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 将相对路径转为绝对路径
            Path uploadPath = Paths.get(dziUploadDir).toAbsolutePath().normalize();
            File targetDir = uploadPath.toFile();
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            // 保存上传的 ZIP 文件到临时位置
            String originalFilename = file.getOriginalFilename();
            log.info("正在上传文件： " + originalFilename);
            Path tempZipPath = Files.createTempFile("dzi-upload-", originalFilename);
            Files.copy(file.getInputStream(), tempZipPath, StandardCopyOption.REPLACE_EXISTING);

            // 解压 ZIP 到目标目录
            unzip(tempZipPath.toFile(), targetDir);

            log.info("文件 " + originalFilename + " 上传完成");

            // 删除临时 ZIP 文件
            Files.delete(tempZipPath);

            // 假设 ZIP 文件中有一个 DZI 描述文件（xml格式），获取第一个 .xml 文件
            File[] xmlFiles = targetDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
            if (xmlFiles == null || xmlFiles.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"未找到 DZI 描述文件\"}");
            }
            String descriptorFilename = xmlFiles[0].getName();

            return ResponseEntity.ok().body("{\"processedDziUrl\": \"" + descriptorFilename + "\"}");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"DZI 文件上传或解压失败\"}");
        }
    }

    /**
     * 解压 ZIP 文件到指定目录
     */
    private void unzip(File zipFile, File targetDir) throws IOException {
        // 使用 BufferedInputStream，并设置缓冲区大小为 64KB
        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile), 64 * 1024), Charset.forName("GBK"))) {
            ZipEntry entry;
            byte[] buffer = new byte[64 * 1024];
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = newFile(targetDir, entry);
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // 确保父目录存在
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * 防止 Zip Slip 漏洞，确保解压路径安全
     */
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Zip entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    /**
     * 提供静态资源访问的接口（例如 DZI 描述文件或 tile 图片）
     */
    @GetMapping("/processed/{fileName:.+}")
    public ResponseEntity<Resource> getDziFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(dziUploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok().body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
