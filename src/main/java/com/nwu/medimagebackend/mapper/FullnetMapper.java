package com.nwu.medimagebackend.mapper;

import com.nwu.medimagebackend.entity.FullnetResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Fullnet分析结果数据库操作接口
 * 
 * @author MedImage团队
 */
@Mapper
public interface FullnetMapper {

    /**
     * 保存Fullnet分析结果
     * 
     * @param result 分析结果
     * @return 影响的行数
     */
    @Insert({
        "INSERT INTO fullnet_results (",
        "filename, result_image_path, overlay_image_path, cell_count,", 
        "cell_area, total_area, cell_ratio, avg_cell_size, analysis_time, task_id)",
        "VALUES (",
        "#{filename}, #{resultImagePath}, #{overlayImagePath}, #{cellCount},", 
        "#{cellArea}, #{totalArea}, #{cellRatio}, #{avgCellSize}, #{analysisTime}, #{taskId})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFullnetResult(FullnetResult result);
    
    /**
     * 根据ID查询分析结果
     * 
     * @param id 结果ID
     * @return 分析结果
     */
    @Select("SELECT * FROM fullnet_results WHERE id = #{id}")
    FullnetResult findById(Long id);
    
    /**
     * 根据任务ID查询分析结果
     * 
     * @param taskId 任务ID
     * @return 分析结果
     */
    @Results({
            @Result(property = "resultImagePath", column = "result_image_path"),
            @Result(property = "overlayImagePath", column = "overlay_image_path"),
            @Result(property = "cellCount", column = "cell_count"),
            @Result(property = "cellArea", column = "cell_area"),
            @Result(property = "totalArea", column = "total_area"),
            @Result(property = "cellRatio", column = "cell_ratio"),
            @Result(property = "avgCellSize", column = "avg_cell_size"),
            @Result(property = "analysisTime", column = "analysis_time"),
            @Result(property = "taskId", column = "task_id")
    })
    @Select("SELECT * FROM fullnet_results WHERE task_id = #{taskId}")
    FullnetResult findByTaskId(String taskId);
    
    /**
     * 根据文件名查询分析结果
     * 
     * @param filename 文件名
     * @return 分析结果
     */
    @Select("SELECT * FROM fullnet_results WHERE filename = #{filename}")
    FullnetResult findByFilename(String filename);
    
    /**
     * 获取所有分析结果
     * 
     * @return 分析结果列表
     */
    @Select("SELECT * FROM fullnet_results ORDER BY analysis_time DESC")
    List<FullnetResult> findAll();
    
    /**
     * 更新分析结果
     * 
     * @param result 分析结果
     * @return 影响的行数
     */
    @Update({
        "UPDATE fullnet_results SET ",
        "result_image_path = #{resultImagePath},",
        "overlay_image_path = #{overlayImagePath},",
        "cell_count = #{cellCount},",
        "cell_area = #{cellArea},",
        "total_area = #{totalArea},",
        "cell_ratio = #{cellRatio},",
        "avg_cell_size = #{avgCellSize},",
        "analysis_time = #{analysisTime},",
        "task_id = #{taskId}",
        "WHERE id = #{id}"
    })
    int updateFullnetResult(FullnetResult result);
    
    /**
     * 根据任务ID更新分析结果
     * 
     * @param result 分析结果
     * @return 影响的行数
     */
    @Update({
        "UPDATE fullnet_results SET ",
        "task_id = #{taskId}",
        "WHERE id = #{id}"
    })
    int updateTaskId(FullnetResult result);
} 