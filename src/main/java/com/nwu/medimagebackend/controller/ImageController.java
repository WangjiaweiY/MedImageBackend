package com.nwu.medimagebackend.controller;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*")  // 允许跨域访问
@Slf4j
public class ImageController {

    private static final String UPLOAD_DIR = "uploads/";
    private final ImageJ imageJ;

    public ImageController() {
        this.imageJ = new ImageJ();
    }

    // 处理图片上传
    @PostMapping("/upload")
    public ResponseEntity<?> handleImageUpload(@RequestParam("image") MultipartFile file) {
        try {
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            // 生成基于日期时间的文件名
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String newFileName = timestamp + ".png"; // 保存为 PNG 格式
            File savedFile = new File(UPLOAD_DIR + File.separator + newFileName);

            // 保存文件
            Files.copy(file.getInputStream(), savedFile.toPath());

            // 处理图像为黑白
            String processedFileName = "bw_" + newFileName;
            File processedFile = new File(UPLOAD_DIR + File.separator + processedFileName);
            convertToBlackWhite(savedFile, processedFile);

            // 返回处理后图片的 URL
            return ResponseEntity.ok().body("{\"processedImageUrl\": \"" + processedFileName + "\"}");


        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("图片处理失败");
        }
    }

    // 提供处理后图片的访问 URL
    @GetMapping("/processed/{fileName}")
    public ResponseEntity<Resource> getProcessedImage(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
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

    // 使用 ImageJ 2.16 将图片转换为黑白
    private void convertToBlackWhite(File inputFile, File outputFile) throws IOException {
        try {
            // 读取图像
            BufferedImage originalImage = ImageIO.read(inputFile);

            // 创建灰度图
            BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

            // 将原始图像绘制到灰度图上
            Graphics2D g = grayImage.createGraphics();
            g.drawImage(originalImage, 0, 0, null);
            g.dispose();

            // 保存处理后的图像
            ImageIO.write(grayImage, "png", outputFile);

        } catch (Exception e) {
            throw new IOException("图像处理失败", e);
        }
    }
}

