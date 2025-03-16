package com.nwu.medimagebackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FileInfo {
    private String folderName;
    private List<String> fileNames;

    public FileInfo(String folderName) {
        this.folderName = folderName;
    }
}