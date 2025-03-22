# MedImageBackend

# Registration 接口文档

**Base URL:** `/api/svs`

------

## 1. 上传配准图像文件夹

- **接口地址:** `/upload`

- **请求方式:** `POST`

- **描述:** 接收上传的配准图像文件夹（多个文件），并进行处理上传。

- **请求参数:**

  - **files** (Request Parameter, MultipartFile[]): 要上传的文件数组

- **请求示例 (curl):**

  ```
  bash复制编辑curl -X POST "http://<server>/api/svs/upload" \
       -F "files=@/path/to/file1" \
       -F "files=@/path/to/file2"
  ```

- **响应示例:**

  ```
  json复制编辑{
    "status": "success",
    "message": "上传成功",
    "data": {
      // 其他返回信息
    }
  }
  ```

- **返回状态:**

  - `200 OK`: 上传成功，返回上传结果信息
  - `500 Internal Server Error`: 上传失败，返回错误信息（包含 `error` 字段）

------

## 2. 获取上传目录下的文件夹信息

- **接口地址:** `/list`

- **请求方式:** `GET`

- **描述:** 列出上传目录下的所有文件夹信息。

- **请求参数:** 无

- **响应示例:**

  ```
  json复制编辑[
    {
      "id": 1,
      "name": "folder1",
      "path": "/path/to/folder1"
      // 其他 FileInfo 字段...
    },
    {
      "id": 2,
      "name": "folder2",
      "path": "/path/to/folder2"
      // 其他 FileInfo 字段...
    }
  ]
  ```

- **返回状态:**

  - `200 OK`: 成功返回文件夹列表

------

## 3. 调用外部配准服务处理指定文件夹的配准操作

- **接口地址:** `/register/{folder}`

- **请求方式:** `POST`

- **描述:** 调用外部配准服务对指定文件夹进行配准操作。

- **请求参数:**

  - **Path Parameter:** `folder` (String) — 指定要进行配准操作的文件夹名称
  - **Request Body:** JSON 对象，包含：
    - `username` (String): 用户名，用于标识或调用外部配准服务

- **请求示例 (curl):**

  ```
  bash复制编辑curl -X POST "http://<server>/api/svs/register/folder1" \
       -H "Content-Type: application/json" \
       -d '{"username": "your_username"}'
  ```

- **响应示例:**

  ```
  json复制编辑{
    "status": "success",
    "message": "配准操作已开始",
    "data": {
      // 其他返回信息
    }
  }
  ```

- **返回状态:**

  - `200 OK`: 成功返回配准结果信息
  - `500 Internal Server Error`: 配准操作失败，返回错误信息（包含 `error` 字段）



# DZI 接口文档

**Base URL:** `/api/dzi`

------

## 1. 获取 DZI 文件列表

- **接口地址:** `/list`

- **请求方式:** `GET`

- **描述:** 获取所有 DZI 文件的列表。

- **请求参数:** 无

- **响应示例:**

  ```
  json复制编辑[
    {
      "id": 1,
      "fileName": "example.dzi",
      "filePath": "/path/to/example.dzi",
      // 其他 FileInfo 字段...
    },
    {
      "id": 2,
      "fileName": "sample.dzi",
      "filePath": "/path/to/sample.dzi",
      // 其他 FileInfo 字段...
    }
  ]
  ```

- **返回状态:**

  - `200 OK`：成功返回文件列表

------

## 2. 静态资源访问（DZI 描述文件或 tile 图片）

- **接口地址:** `/processed/**`
- **请求方式:** `GET`
- **描述:** 提供静态资源访问，用于获取 DZI 描述文件或 tile 图片。请求 URL 中 `/processed/` 后面的部分将作为资源路径解析。
- **请求参数:** 无（资源路径通过 URL 动态解析）
- **响应示例:**
  - 返回对应的静态资源文件（例如图片、XML 文件等）
- **返回状态:**
  - `200 OK`：成功返回资源文件
  - `500 Internal Server Error`：文件获取异常

------

## 3. 获取指定文件夹下的文件列表

- **接口地址:** `/list/{folderName}`

- **请求方式:** `GET`

- **描述:** 根据文件夹名称获取该文件夹下的所有文件（FileItem 列表）。

- **请求参数:**

  - **Path Parameter:** `folderName` - 文件夹名称

- **响应示例:**

  ```
  json复制编辑[
    {
      "fileName": "image1.jpg",
      "fileSize": "500KB",
      // 其他 FileItem 字段...
    },
    {
      "fileName": "image2.jpg",
      "fileSize": "600KB",
      // 其他 FileItem 字段...
    }
  ]
  ```

- **返回状态:**

  - `200 OK`：成功返回文件列表
  - `404 Not Found`：文件夹下无文件或不存在

------

## 4. 删除文件夹

- **接口地址:** `/deleteFolder/{folderName}`
- **请求方式:** `DELETE`
- **描述:** 删除指定名称的文件夹。
- **请求参数:**
  - **Path Parameter:** `folderName` - 文件夹名称
- **响应示例:** 无
- **返回状态:**
  - `200 OK`：文件夹删除成功
  - `500 Internal Server Error`：文件夹删除失败

------

## 5. 删除文件

- **接口地址:** `/delete/{folderName}/{fileName}`
- **请求方式:** `DELETE`
- **描述:** 删除指定文件夹下的指定文件。
- **请求参数:**
  - **Path Parameter:** `folderName` - 文件夹名称
  - **Path Parameter:** `fileName` - 文件名称
- **响应示例:** 无
- **返回状态:**
  - `200 OK`：文件删除成功
  - `500 Internal Server Error`：文件删除失败

# IHC 分析接口文档

**Base URL:** `/api/ihc`

------

## 1. 图像分析

- **接口地址:** `/analyze`

- **请求方式:** `POST`

- **描述:** 对指定文件夹下的指定文件进行免疫组化图像分析。

- **请求参数:**

  - **folderName** (Request Parameter, String): 文件夹名称
  - **fileName** (Request Parameter, String): 文件名称

- **响应示例:**

  ```
  json复制编辑{
    "analysisId": 123,
    "result": "positive",
    "details": "详细的分析信息..."
  }
  ```

- **返回状态:**

  - `200 OK`：成功返回分析结果
  - `500 Internal Server Error`：分析过程中发生异常

------

## 2. 查询单个文件的分析结果

- **接口地址:** `/result`

- **请求方式:** `GET`

- **描述:** 根据文件夹名称和文件名称查询对应的免疫组化分析结果。

- **请求参数:**

  - **folderName** (Request Parameter, String): 文件夹名称
  - **fileName** (Request Parameter, String): 文件名称

- **响应示例:**

  ```
  json复制编辑{
    "analysisId": 123,
    "result": "positive",
    "details": "详细的分析信息..."
  }
  ```

- **返回状态:**

  - `200 OK`：成功返回分析结果
  - `404 Not Found`：未找到对应的分析结果
  - `500 Internal Server Error`：查询过程中发生异常

------

## 3. 查询文件夹内所有分析结果

- **接口地址:** `/resultfolder`

- **请求方式:** `GET`

- **描述:** 根据文件夹名称查询该文件夹下所有文件的免疫组化分析结果。

- **请求参数:**

  - **folderName** (Request Parameter, String): 文件夹名称

- **响应示例:**

  ```
  json复制编辑[
    {
      "analysisId": 123,
      "result": "positive",
      "details": "详细的分析信息..."
    },
    {
      "analysisId": 124,
      "result": "negative",
      "details": "详细的分析信息..."
    }
  ]
  ```

- **返回状态:**

  - `200 OK`：成功返回分析结果列表
  - `404 Not Found`：指定文件夹内无分析结果
  - `500 Internal Server Error`：查询过程中发生异常

# User 接口文档

**Base URL:** `/api/user`

------

## 1. 用户登录

- **接口地址:** `/login`

- **请求方式:** `POST`

- **描述:** 用户登录接口，通过提交用户信息进行登录。请求体中应包含用户名和密码等必要字段。

- **请求参数:**

  - **Request Body:** JSON 对象，示例字段：
    - `username` (String) — 用户名
    - `password` (String) — 密码

- **请求示例:**

  ```
  json复制编辑{
    "username": "exampleUser",
    "password": "examplePassword"
  }
  ```

- **响应说明:**

  - **200 OK:** 登录成功，返回用户信息对象。
  - **401 Unauthorized:** 登录失败，返回提示信息 "用户名或密码错误"。
  - **400 Bad Request:** 请求异常，返回具体错误信息。

- **响应示例 (成功):**

  ```
  json复制编辑{
    "id": 1,
    "username": "exampleUser",
    "email": "user@example.com"
    // 其他用户属性...
  }
  ```

- **响应示例 (失败):**

  ```
  json
  
  
  复制编辑
  "用户名或密码错误"
  ```

------

## 2. 用户注册

- **接口地址:** `/register`

- **请求方式:** `POST`

- **描述:** 用户注册接口，通过提交用户信息进行注册。请求体中应包含必要的注册信息。

- **请求参数:**

  - **Request Body:** JSON 对象，示例字段：
    - `username` (String) — 用户名
    - `password` (String) — 密码
    - `email` (String) — 邮箱（如果需要）

- **请求示例:**

  ```
  json复制编辑{
    "username": "newUser",
    "password": "newPassword",
    "email": "newuser@example.com"
  }
  ```

- **响应说明:**

  - **200 OK:** 注册成功，返回提示信息 "注册成功"。
  - **400 Bad Request:** 请求异常或注册失败，返回具体错误信息。

- **响应示例 (成功):**

  ```
  json
  
  
  复制编辑
  "注册成功"
  ```

- **响应示例 (失败):**

  ```
  json
  
  
  复制编辑
  "错误信息描述"
  ```