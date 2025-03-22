package com.nwu.medimagebackend.mapper;

import com.nwu.medimagebackend.entity.IhcAnalysisResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;

@Mapper
public interface RegistrationMapper {
    @Insert("INSERT INTO ihcs (foldername, image_name, username, analysis_date, positive_area, total_area, positive_ratio) VALUES (#{folderName}, #{imageName}, #{userName}, #{analysisDate}, #{positiveArea}, #{totalArea}, #{positiveRatio})")
    void insertFileInfo(IhcAnalysisResult fileInfo);
}
