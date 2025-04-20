# MedImage 医学图像分析后端系统

## 项目介绍

MedImage是一个医学图像分析系统的后端部分，提供对医学图像（特别是免疫组化图像）的处理、分析和管理功能。系统支持图像上传、自动分析、结果存储和查询等核心功能。

### 主要功能

- **图像上传和管理**：支持SVS等格式的医学图像上传和管理
- **免疫组化图像分析**：自动分析免疫组化图像中的阳性染色区域，计算阳性率
- **数据存储和查询**：将分析结果存储在数据库中，支持按文件夹和文件名查询
- **图像资源访问**：提供图像和缩略图的静态资源访问

## 环境要求

- JDK 11+
- MySQL 5.7+
- Maven 3.6+
- 磁盘空间：至少5GB（用于图像存储）

## 快速开始

### 1. 配置数据库

创建MySQL数据库并执行初始化脚本：

```sql
CREATE DATABASE med_image_analyse;
USE med_image_analyse;
-- 执行项目根目录下的schema.sql文件创建表结构
```

### 2. 配置应用

根据实际环境修改配置文件（`src/main/resources/application.yml`）中的数据库连接信息和文件存储路径。

### 3. 构建和运行

```bash
# 构建项目
mvn clean package

# 运行应用（开发环境）
java -jar target/medimagebackend.jar --spring.profiles.active=dev

# 运行应用（生产环境）
java -jar target/medimagebackend.jar --spring.profiles.active=prod
```

## 项目结构

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── nwu/
│   │   │           └── medimagebackend/
│   │   │               ├── config/        # 配置类
│   │   │               ├── controller/    # 控制器，处理HTTP请求
│   │   │               ├── DTO/           # 数据传输对象
│   │   │               ├── entity/        # 实体类
│   │   │               ├── mapper/        # 数据访问层
│   │   │               ├── service/       # 服务层
│   │   │               │   └── impl/      # 服务实现类
│   │   │               ├── utils/         # 工具类
│   │   │               └── MedimagebackendApplication.java  # 应用入口
│   │   └── resources/
│   │       ├── static/         # 静态资源
│   │       ├── application.yml # 主配置文件
│   │       ├── application-dev.yml  # 开发环境配置
│   │       └── application-prod.yml # 生产环境配置
│   └── test/                  # 测试代码
├── pom.xml                    # Maven配置
└── README.md                  # 项目说明文档
```

## 配置说明

系统配置分为三个层次：

1. **基础配置**（`application.yml`）：包含通用配置，如数据库连接、上传限制等
2. **开发环境配置**（`application-dev.yml`）：开发环境特定配置，如本地路径等
3. **生产环境配置**（`application-prod.yml`）：生产环境特定配置，如服务器路径等

### 关键配置项

- **数据库配置**：
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/med_image_analyse
      username: root
      password: 1234
  ```

- **文件上传目录**：
  ```yaml
  uploads:
    svs:
      dir: "/path/to/svs/uploads/"
    register:
      dir: "/path/to/register_results/"
  ```

- **后端服务URL**：
  ```yaml
  app:
    backend-url: "http://localhost:8080"  # 开发环境
    # backend-url: "https://api.example.com"  # 生产环境
  ```

## API文档

### 1. 用户管理

#### 1.1 用户登录

- **URL**: `/api/user/login`
- **方法**: `POST`
- **Content-Type**: `application/json`
- **请求体格式**:
  ```json
  {
    "username": "test_user",
    "password": "password123"
  }
  ```
- **响应格式**:
  ```json
  {
    "id": 1,
    "username": "test_user",
    "role": "USER"
  }
  ```
- **错误码**:
  - 401: 用户名或密码错误
  - 400: 请求参数错误

#### 1.2 用户注册

- **URL**: `/api/user/register`
- **方法**: `POST`
- **Content-Type**: `application/json`
- **请求体格式**:
  ```json
  {
    "username": "new_user",
    "password": "password123"
  }
  ```
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "注册成功"
  }
  ```
- **错误码**:
  - 400: 注册失败（用户名已存在等）

### 2. 图像上传和处理

#### 2.1 上传SVS图像文件

- **URL**: `/api/svs/upload`
- **方法**: `POST`
- **Content-Type**: `multipart/form-data`
- **参数**: 
  - `files` (File[]) - 多文件上传，支持SVS格式
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "文件上传成功",
    "data": {
      "successCount": 2,
      "failedCount": 0,
      "failedFiles": []
    }
  }
  ```
- **错误码**:
  - 400: 文件格式不支持
  - 500: 服务器内部错误

#### 2.2 获取SVS文件列表

- **URL**: `/api/svs/list`
- **方法**: `GET`
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": [
      {
        "folderName": "folder1",
        "fileCount": 5,
        "createTime": "2024-03-20T10:30:00"
      }
    ]
  }
  ```

#### 2.3 调用配准服务

- **URL**: `/api/svs/register/{folder}`
- **方法**: `POST`
- **Content-Type**: `application/json`
- **参数**: 
  - `folder` (String) - 文件夹名称（路径参数）
  - `username` (String) - 用户名（请求体）
- **请求体格式**:
  ```json
  {
    "username": "test_user"
  }
  ```
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "配准服务调用成功",
    "data": {
      "taskId": "task_123456",
      "status": "processing"
    }
  }
  ```
- **错误码**:
  - 400: 参数错误
  - 404: 文件夹不存在
  - 500: 服务器内部错误

### 3. DZI图像管理

#### 3.1 获取DZI文件列表

- **URL**: `/api/dzi/list`
- **方法**: `GET`
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": [
      {
        "folderName": "folder1",
        "fileCount": 5,
        "createTime": "2024-03-20T10:30:00"
      }
    ]
  }
  ```

#### 3.2 获取DZI资源文件

- **URL**: `/api/dzi/processed/**`
- **方法**: `GET`
- **说明**: 用于获取DZI描述文件(.dzi)和瓦片图像(.jpg/.png)
- **响应**: 返回请求的资源文件

#### 3.3 获取文件夹内容

- **URL**: `/api/dzi/list/{folderName}`
- **方法**: `GET`
- **参数**:
  - `folderName` (String) - 文件夹名称（路径参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": [
      {
        "fileName": "image1.dzi",
        "fileSize": 1024,
        "createTime": "2024-03-20T10:30:00"
      }
    ]
  }
  ```

#### 3.4 删除文件夹

- **URL**: `/api/dzi/deleteFolder/{folderName}`
- **方法**: `DELETE`
- **参数**:
  - `folderName` (String) - 文件夹名称（路径参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "删除成功"
  }
  ```

#### 3.5 删除文件

- **URL**: `/api/dzi/delete/{folderName}/{fileName}`
- **方法**: `DELETE`
- **参数**:
  - `folderName` (String) - 文件夹名称（路径参数）
  - `fileName` (String) - 文件名称（路径参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "删除成功"
  }
  ```

### 4. IHC分析

#### 4.1 分析图像

- **URL**: `/api/ihc/analyze`
- **方法**: `POST`
- **Content-Type**: `application/json`
- **参数**:
  - `folderName` (String) - 文件夹名称
  - `fileName` (String) - 文件名
- **请求体格式**:
  ```json
  {
    "folderName": "test_folder",
    "fileName": "test_image.svs"
  }
  ```
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "分析任务已启动",
    "data": {
      "taskId": "task_789012",
      "status": "processing"
    }
  }
  ```
- **错误码**:
  - 400: 参数错误
  - 404: 文件不存在
  - 500: 服务器内部错误

#### 4.2 获取分析结果

- **URL**: `/api/ihc/result`
- **方法**: `GET`
- **参数**:
  - `folderName` (String) - 文件夹名称（查询参数）
  - `fileName` (String) - 文件名（查询参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "获取结果成功",
    "data": {
      "folderName": "test_folder",
      "fileName": "test_image.svs",
      "positiveRate": 0.75,
      "analysisTime": "2024-03-20T10:30:00",
      "status": "completed"
    }
  }
  ```
- **错误码**:
  - 400: 参数错误
  - 404: 结果不存在
  - 500: 服务器内部错误

#### 4.3 获取文件夹下的所有结果

- **URL**: `/api/ihc/resultfolder`
- **方法**: `GET`
- **参数**:
  - `folderName` (String) - 文件夹名称（查询参数）
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "获取结果成功",
    "data": [
      {
        "folderName": "test_folder",
        "fileName": "image1.svs",
        "positiveRate": 0.75,
        "analysisTime": "2024-03-20T10:30:00",
        "status": "completed"
      }
    ]
  }
  ```
- **错误码**:
  - 400: 参数错误
  - 404: 文件夹不存在
  - 500: 服务器内部错误

#### 4.4 调整阈值并分析图像

- **URL**: `/api/ihc/threshold-analysis`
- **方法**: `POST`
- **Content-Type**: `application/json`
- **参数**:
  - `folderName` (String) - 文件夹名称
  - `fileName` (String) - 文件名
  - `threshold` (Integer) - 阈值（0-255之间的整数值）
- **请求体格式**:
  ```json
  {
    "folderName": "test_folder",
    "fileName": "test_image.svs",
    "threshold": 128
  }
  ```
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "阈值分析成功",
    "data": {
      "folderName": "test_folder",
      "fileName": "test_image.svs",
      "threshold": 128,
      "positiveRate": 0.67,
      "imageUrl": "/api/ihc/result-image/test_folder/test_image_128.jpg",
      "analysisTime": "2024-03-20T10:30:00"
    }
  }
  ```
- **说明**: 
  - 该接口用于根据指定的阈值对图像进行分析
  - 阈值值范围为0-255，代表图像像素强度的阈值
  - 返回的imageUrl包含了分析后的图像，其中大于阈值的区域被标红
  - 每次调整阈值都会生成新的分析结果和对应的结果图像
- **错误码**:
  - 400: 参数错误（如阈值超出范围）
  - 404: 文件不存在
  - 500: 服务器内部错误

#### 4.5 获取阈值分析结果图像

- **URL**: `/api/ihc/result-image/{folderName}/{fileName}`
- **方法**: `GET`
- **参数**:
  - `folderName` (String) - 文件夹名称（路径参数）
  - `fileName` (String) - 结果图像文件名（路径参数）
- **响应**: 返回阈值分析后的结果图像（JPEG/PNG格式）
- **说明**:
  - 该接口用于获取阈值分析生成的结果图像
  - 文件名格式通常为原始文件名加阈值后缀，如`test_image_128.jpg`
- **错误码**:
  - 404: 结果图像不存在
  - 500: 服务器内部错误

### 5. 通用响应格式

所有API响应都遵循以下格式：

```json
{
  "code": 200,          // 状态码
  "message": "成功",    // 响应消息
  "data": {}           // 响应数据
}
```

### 6. 通用错误码

- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器内部错误
- 503: 服务不可用

## 开发指南

### 环境搭建

1. 克隆代码仓库
2. 安装JDK 11+和Maven 3.6+
3. 在IDE中导入项目（推荐使用IntelliJ IDEA）
4. 安装并配置MySQL数据库
5. 创建相应的文件目录用于存储上传的图像

### 编码规范

- 使用Lombok减少样板代码
- 所有公共API需添加Javadoc注释
- 遵循RESTful API设计原则
- 使用统一的响应格式
- 使用PathUtils进行路径处理

## 贡献者

- MedImage团队

## 许可证

[MIT License](LICENSE)