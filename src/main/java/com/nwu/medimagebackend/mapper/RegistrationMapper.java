package com.nwu.medimagebackend.mapper;

import com.nwu.medimagebackend.entity.IhcAnalysisResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;

@Mapper
public interface RegistrationMapper {
    @Insert("INSERT INTO ihcs (foldername, image_name, username, positive_area, total_area, positive_ratio, uploads_date) VALUES (#{folderName}, #{imageName}, #{userName}, #{positiveArea}, #{totalArea}, #{positiveRatio}, #{uploadsDate})")
    void insertFileInfo(IhcAnalysisResult fileInfo);
}
