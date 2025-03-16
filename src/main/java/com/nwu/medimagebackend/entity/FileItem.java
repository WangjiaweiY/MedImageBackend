package com.nwu.medimagebackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileItem {
    private String name;
    private boolean directory; // true 表示目录，false 表示文件

}