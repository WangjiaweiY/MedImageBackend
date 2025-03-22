package com.nwu.medimagebackend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper

public interface DziMapper {
    @Delete("DELETE FROM ihc_analysis_result WHERE foldername = #{foldername}")
    int deleteByFoldername(@Param("foldername") String foldername);

    @Delete("DELETE FROM ihc_analysis_result WHERE foldername = #{foldername} and image_name = #{filename}")
    int deleteByFilename(@Param("foldername") String foldername, @Param("filename") String filename);
}
