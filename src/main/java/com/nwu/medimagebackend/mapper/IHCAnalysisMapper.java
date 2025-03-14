package com.nwu.medimagebackend.mapper;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IHCAnalysisMapper {
    @Insert("INSERT INTO ihc_analysis_result(image_name, positive_area, total_area, analysis_date) " +
            "VALUES(#{imageName}, #{positiveArea}, #{totalArea}, #{analysisDate})")
    int insert(IhcAnalysisResult result);
}
