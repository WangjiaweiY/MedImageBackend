package com.nwu.medimagebackend.mapper;

import com.nwu.medimagebackend.entity.FullnetTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Fullnet异步任务数据库操作接口
 * 
 * @author MedImage团队
 */
@Mapper
public interface FullnetTaskMapper {

    /**
     * 保存任务
     * 
     * @param task 任务对象
     * @return 影响的行数
     */
    @Insert({
        "INSERT INTO fullnet_tasks (",
        "id, filename, actual_filename, status, progress,", 
        "error_message, created_time, completed_time, result_id)",
        "VALUES (",
        "#{id}, #{filename}, #{actualFilename}, #{status}, #{progress},", 
        "#{errorMessage}, #{createdTime}, #{completedTime}, #{resultId})"
    })
    int insertTask(FullnetTask task);
    
    /**
     * 根据ID查询任务
     * 
     * @param id 任务ID
     * @return 任务对象
     */
    @Select("SELECT * FROM fullnet_tasks WHERE id = #{id}")
    FullnetTask findById(String id);
    
    /**
     * 根据文件名查询最新任务
     * 
     * @param filename 文件名
     * @return 任务对象
     */
    @Select("SELECT * FROM fullnet_tasks WHERE filename = #{filename} ORDER BY created_time DESC LIMIT 1")
    FullnetTask findLatestByFilename(String filename);
    
    /**
     * 获取所有任务
     * 
     * @return 任务列表
     */
    @Select("SELECT * FROM fullnet_tasks ORDER BY created_time DESC")
    List<FullnetTask> findAll();
    
    /**
     * 更新任务状态
     * 
     * @param task 任务对象
     * @return 影响的行数
     */
    @Update({
        "UPDATE fullnet_tasks SET ",
        "status = #{status},",
        "progress = #{progress},",
        "error_message = #{errorMessage},",
        "completed_time = #{completedTime},",
        "result_id = #{resultId}",
        "WHERE id = #{id}"
    })
    int updateTask(FullnetTask task);
} 