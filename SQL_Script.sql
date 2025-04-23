-- fullnet_results表
CREATE TABLE IF NOT EXISTS fullnet_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    result_image_path VARCHAR(500),
    overlay_image_path VARCHAR(500),
    cell_count INT,
    cell_area INT,
    total_area INT,
    cell_ratio DOUBLE,
    avg_cell_size DOUBLE,
    analysis_time DATETIME,
    task_id VARCHAR(36) COMMENT '关联的异步任务ID',
    UNIQUE KEY idx_filename (filename),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- fullnet_tasks表
CREATE TABLE IF NOT EXISTS fullnet_tasks (
    id VARCHAR(36) PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    actual_filename VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    progress VARCHAR(255),
    error_message TEXT,
    created_time DATETIME NOT NULL,
    completed_time DATETIME,
    result_id BIGINT,
    INDEX idx_filename (filename),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加注释
ALTER TABLE fullnet_results 
MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '主键ID',
MODIFY COLUMN filename VARCHAR(255) NOT NULL COMMENT '分析的文件名',
MODIFY COLUMN result_image_path VARCHAR(500) COMMENT '结果图像路径',
MODIFY COLUMN overlay_image_path VARCHAR(500) COMMENT '叠加图像路径',
MODIFY COLUMN cell_count INT COMMENT '细胞数量',
MODIFY COLUMN cell_area INT COMMENT '细胞面积',
MODIFY COLUMN total_area INT COMMENT '总面积',
MODIFY COLUMN cell_ratio DOUBLE COMMENT '细胞比例(%)',
MODIFY COLUMN avg_cell_size DOUBLE COMMENT '平均细胞大小',
MODIFY COLUMN analysis_time DATETIME COMMENT '分析完成时间',
MODIFY COLUMN task_id VARCHAR(36) COMMENT '关联的异步任务ID';

ALTER TABLE fullnet_tasks
MODIFY COLUMN id VARCHAR(36) COMMENT '任务ID',
MODIFY COLUMN filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
MODIFY COLUMN actual_filename VARCHAR(255) COMMENT '实际处理的文件名（含后缀）',
MODIFY COLUMN status VARCHAR(20) NOT NULL COMMENT '任务状态：PENDING（等待中）, PROCESSING（处理中）, COMPLETED（已完成）, FAILED（失败）',
MODIFY COLUMN progress VARCHAR(255) COMMENT '进度描述',
MODIFY COLUMN error_message TEXT COMMENT '错误信息（如果有）',
MODIFY COLUMN created_time DATETIME NOT NULL COMMENT '任务创建时间',
MODIFY COLUMN completed_time DATETIME COMMENT '任务完成时间',
MODIFY COLUMN result_id BIGINT COMMENT '关联的分析结果ID';

-- 为已有的表添加任务ID字段（如果表已存在但没有task_id字段）
ALTER TABLE fullnet_results 
ADD COLUMN IF NOT EXISTS task_id VARCHAR(36) COMMENT '关联的异步任务ID',
ADD INDEX IF NOT EXISTS idx_task_id (task_id); 