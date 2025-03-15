package com.nwu.medimagebackend.mapper;

import com.nwu.medimagebackend.common.IhcAnalysisResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IHCAnalysisMapper {
    @Insert("INSERT INTO ihc_analysis_result(image_name, positive_area, total_area, analysis_date, foldername, positive_ratio) " +
            "VALUES(#{imageName}, #{positiveArea}, #{totalArea}, #{analysisDate}, #{folderName}, #{positiveRatio})")
    int insert(IhcAnalysisResult result);

    @Select("SELECT * FROM ihc_analysis_result WHERE foldername = #{folderName} AND image_name = #{fileName}")
    @Results({
            @Result(property = "folderName", column = "foldername"),
            @Result(property = "imageName", column = "image_name"),
            @Result(property = "positiveArea", column = "positive_area"),
            @Result(property = "totalArea", column = "total_area"),
            @Result(property = "positiveRatio", column = "positive_ratio"),
            @Result(property = "analysisDate", column = "analysis_date")
    })
    IhcAnalysisResult findByImageName(@Param("folderName") String folderName, @Param("fileName") String fileName);


    @Select("SELECT * FROM ihc_analysis_result WHERE foldername = #{folderName}")
    @Results({
            @Result(property = "folderName", column = "foldername"),
            @Result(property = "imageName", column = "image_name"),
            @Result(property = "positiveArea", column = "positive_area"),
            @Result(property = "totalArea", column = "total_area"),
            @Result(property = "positiveRatio", column = "positive_ratio"),
            @Result(property = "analysisDate", column = "analysis_date")
    })
    List<IhcAnalysisResult> findByFolderName(String folderName);

}
