# QStoryPlugin 公开 API 接口文档

> 面向 AI 助手和开发者的完整接口参考文档。本文档包含所有响应字段的详细说明，可据此开发前端或进行接口对接。

---

## 概述

| 项目               | 说明                                                             |
|------------------|----------------------------------------------------------------|
| **Base URL**     | `https://plugin.suzhelan.top/api/plugin/public`                |
| **认证方式**         | 无需认证，所有接口公开访问                                                  |
| **限流规则**         | IP 维度限流，每秒最多 10 次请求                                            |
| **协议**           | HTTP REST，所有请求方法为 GET                                          |
| **Content-Type** | 响应统一为 `application/json`（下载接口除外，返回 `application/octet-stream`） |

---

## 统一响应格式

所有 JSON 接口的响应体都遵循以下结构：

```json
{
  "status": 200,
  "message": "success",
  "data": {
    ...
  },
  "timestamp": 1705312200000
}
```

### 响应字段说明

| 字段          | 类型                          | 说明                                          |
|-------------|-----------------------------|---------------------------------------------|
| `status`    | `integer`                   | 业务状态码。**注意：HTTP 状态码始终为 200**，业务成功/失败通过此字段判断 |
| `message`   | `string`                    | 状态描述。成功时为 `"success"`，失败时为错误描述信息            |
| `data`      | `object` / `array` / `null` | 响应数据，失败时为 `null`。具体结构取决于各接口                 |
| `path`      | `string`                    | 请求路径。**仅错误响应时存在**                           |
| `timestamp` | `integer`                   | 服务器响应时的 Unix 毫秒时间戳                          |

### 错误响应示例

```json
{
  "status": 500,
  "message": "脚本不存在",
  "data": null,
  "path": "/api/plugin/public/scripts/9999999999",
  "timestamp": 1705312200000
}
```

### 重要说明：bigint 类型

`cloudId` 字段在数据库中的类型为 PostgreSQL `bigint`。在 JSON 响应中，由于 JavaScript 精度限制，bigint 以**字符串形式传输
**。

```
数据库存储: 7389201847563821056 (bigint)
JSON 传输:  "7389201847563821056" (string)
```

前端开发时，将 `cloudId` 作为字符串处理即可，传回给后端时也以字符串形式放在路径参数中。

---

## 枚举值参考

### 审核状态 (AuditStatus)

| 值   | 名称    | 含义                           |
|-----|-------|------------------------------|
| `0` | 待审核   | 脚本已上传，等待管理员审核                |
| `1` | 审核通过  | 管理员已批准，脚本可以上线                |
| `2` | 审核未通过 | 管理员拒绝，`auditReason` 字段包含拒绝原因 |

### 在线状态 (OnlineStatus)

| 值    | 名称  | 含义                                  |
|------|-----|-------------------------------------|
| `-1` | 未上线 | 脚本没有 `online_plugin` 记录（从未上线或记录已删除） |
| `0`  | 已下架 | 曾上线但被管理员下架                          |
| `1`  | 在线  | 当前在插件市场可见且可下载                       |

### AI 评审状态 (AiReviewStatus)

| 值   | 名称   | 含义                       |
|-----|------|--------------------------|
| `0` | 评审中  | AI 正在分析脚本                |
| `1` | 评审成功 | 分析完成，`reviewResult` 字段有值 |
| `2` | 评审失败 | 分析出错，`errorMessage` 字段有值 |

### AI 风险等级 (AiReviewRiskLevel)

| 值          | 名称   | 含义          |
|------------|------|-------------|
| `"low"`    | 低风险  | 脚本安全可靠      |
| `"medium"` | 中等风险 | 存在一些需要注意的问题 |
| `"high"`   | 高风险  | 存在严重安全或合规问题 |

### 文件状态 (FileState)

| 值   | 名称  | 含义     |
|-----|-----|--------|
| `0` | 已删除 | 文件已被清理 |
| `1` | 正常  | 文件可用   |
| `2` | 过期  | 文件已过期  |

### 问题级别 (IssueLevel)

| 值           | 含义                            |
|-------------|-------------------------------|
| `"error"`   | 严重问题 — 可能导致合规性不通过（如安全漏洞、恶意代码） |
| `"warning"` | 警告 — 不影响合规性但建议修改（如代码质量、性能问题）  |
| `"info"`    | 提示 — 仅供参考的信息                  |

### 图片状态

| 值   | 含义       |
|-----|----------|
| `0` | 提取失败或无图片 |
| `1` | 图片提取成功   |

### 标签可选值 (Tag)

| 值        | 说明          |
|----------|-------------|
| `"群聊辅助"` | 群聊管理相关功能    |
| `"娱乐功能"` | 娱乐互动类功能     |
| `"功能扩展"` | 扩展工具类功能     |
| `"综合脚本"` | 包含多种功能的综合脚本 |
| `"官方脚本"` | 官方提供的脚本     |

---

## 接口列表

---

### 1. GET `/api/plugin/public/scripts` — 分页查询所有脚本

查询所有审核状态的脚本列表（包括待审核、已通过、未通过的脚本）。支持多种筛选和排序条件。

#### 请求参数 (Query)

| 参数         | 类型        | 必填 | 默认值      | 说明                                            |
|------------|-----------|----|----------|-----------------------------------------------|
| `page`     | `integer` | 否  | `1`      | 页码，从 1 开始，最小值 1                               |
| `pageSize` | `integer` | 否  | `20`     | 每页数量，最小 1，最大 100（服务端强制限制）                     |
| `sort`     | `string`  | 否  | `"time"` | 排序方式：`"time"` = 按上传时间倒序，`"download"` = 按下载量倒序 |
| `tag`      | `string`  | 否  | `"全部"`   | 标签筛选，可选值见枚举参考。`"全部"` 表示不过滤                    |
| `status`   | `integer` | 否  | `-1`     | 审核状态筛选：`-1` = 全部，`0` = 待审核，`1` = 通过，`2` = 未通过 |
| `keyword`  | `string`  | 否  | `""`     | 模糊搜索关键词，同时匹配名称、描述、作者三个字段                      |

> **注意**：标签筛选在应用层执行，因此当使用 `tag` 过滤时，返回的 `list` 长度可能小于 `pageSize`，但 `total` 是过滤前的总数。

#### 响应示例

```json
{
  "status": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "cloudId": "7389201847563821056",
        "pluginId": "com.example.groupmanager",
        "name": "群管助手",
        "description": "一个强大的群聊管理工具，支持自动踢人、禁言、欢迎语等功能。",
        "author": "张三",
        "version": "1.0.0",
        "type": 1,
        "tags": [
          "群聊辅助",
          "综合脚本"
        ],
        "auditStatus": 1,
        "auditReason": "功能完善，代码规范",
        "processor": "admin",
        "uploadedAt": "2025-01-15T08:30:00.000Z",
        "uploadedBy": "张三",
        "onlineStatus": 1,
        "downloadCount": 1520,
        "images": {
          "id": 1,
          "cloudId": "7389201847563821056",
          "iconStatus": 1,
          "iconFilename": "icon.png",
          "previewStatus": 1,
          "previewFilename": [
            "1.png",
            "2.png"
          ],
          "createdAt": "2025-01-15T08:30:00.000Z"
        }
      }
    ],
    "total": 85,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5
  },
  "timestamp": 1705312200000
}
```

#### 响应字段说明

**外层 `data` 对象（分页信息）：**

| 字段           | 类型                 | 说明                                     |
|--------------|--------------------|----------------------------------------|
| `list`       | `ScriptListItem[]` | 当前页的脚本列表（数组）                           |
| `total`      | `integer`          | 满足筛选条件的总记录数                            |
| `page`       | `integer`          | 当前页码                                   |
| `pageSize`   | `integer`          | 每页数量                                   |
| `totalPages` | `integer`          | 总页数，计算方式 `Math.ceil(total / pageSize)` |

**`list` 数组中的 `ScriptListItem` 对象：**

| 字段              | 类型                      | 说明                                                 |
|-----------------|-------------------------|----------------------------------------------------|
| `cloudId`       | `string`                | 脚本云端唯一 ID（bigint 序列化为字符串），用于其他接口的路径参数              |
| `pluginId`      | `string`                | 插件标识符，格式通常为反向域名（如 `com.example.xxx`），在脚本 ZIP 配置中定义 |
| `name`          | `string`                | 脚本显示名称                                             |
| `description`   | `string`                | 脚本功能描述文本                                           |
| `author`        | `string`                | 脚本作者名称，来自脚本配置                                      |
| `version`       | `string`                | 脚本版本号，遵循 SemVer（如 `1.0.0`）                         |
| `type`          | `integer`               | 脚本类型编号（具体含义由客户端定义）                                 |
| `tags`          | `string[]`              | 分类标签数组，可选值见枚举参考。一个脚本可有多个标签                         |
| `auditStatus`   | `integer`               | 最新审核状态：`0`=待审核，`1`=通过，`2`=未通过                      |
| `auditReason`   | `string`                | 审核意见。通过时为正面评价，拒绝时说明原因                              |
| `processor`     | `string`                | 处理此次审核的管理员用户名                                      |
| `uploadedAt`    | `string`                | 上传时间（ISO 8601 格式，如 `"2025-01-15T08:30:00.000Z"`）   |
| `uploadedBy`    | `string`                | 上传者的 QQ 昵称                                         |
| `onlineStatus`  | `integer`               | 在线状态：`-1`=未上线，`0`=已下架，`1`=在线                       |
| `downloadCount` | `integer`               | 累计下载次数。未上线时为 `0`                                   |
| `images`        | `PluginImages` / `null` | 脚本图片信息，无图片时为 `null`。结构见下方 PluginImages 说明          |

---

### 2. GET `/api/plugin/public/search` — 搜索脚本

根据关键词搜索脚本，匹配名称、描述和作者字段。返回所有状态的脚本（不限于已上线）。响应结构与分页列表接口相同。

#### 请求参数 (Query)

| 参数         | 类型        | 必填    | 默认值  | 说明           |
|------------|-----------|-------|------|--------------|
| `keyword`  | `string`  | **是** | —    | 搜索关键词，最小长度 1 |
| `page`     | `integer` | 否     | `1`  | 页码           |
| `pageSize` | `integer` | 否     | `20` | 每页数量（最大 100） |

#### 响应示例

```json
{
  "status": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "cloudId": "7389201847563821056",
        "pluginId": "com.example.groupmanager",
        "name": "群管助手",
        "description": "一个强大的群聊管理工具",
        "author": "张三",
        "version": "1.0.0",
        "type": 1,
        "tags": [
          "群聊辅助"
        ],
        "auditStatus": 1,
        "auditReason": "功能完善",
        "processor": "admin",
        "uploadedAt": "2025-01-15T08:30:00.000Z",
        "uploadedBy": "张三",
        "onlineStatus": 1,
        "downloadCount": 1520,
        "images": null
      }
    ],
    "total": 3,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1
  },
  "timestamp": 1705312200000
}
```

#### 响应字段说明

与分页列表接口（接口 1）完全相同，参考上面的 `ScriptListItem` 说明。

---

### 3. GET `/api/plugin/public/scripts/:cloudId` — 脚本详情

查询指定脚本的完整详情，包含基础信息、图片、最新审核记录和在线状态。脚本不存在时返回错误。

#### 路径参数

| 参数        | 类型       | 必填 | 说明                       |
|-----------|----------|----|--------------------------|
| `cloudId` | `string` | 是  | 脚本云端 ID（bigint，以字符串形式传入） |

#### 响应示例

```json
{
  "status": 200,
  "message": "success",
  "data": {
    "cloudId": "7389201847563821056",
    "pluginId": "com.example.groupmanager",
    "name": "群管助手",
    "description": "一个强大的群聊管理工具，支持自动踢人、禁言、欢迎语等功能。",
    "author": "张三",
    "version": "1.0.0",
    "type": 1,
    "date": "2025-01-15",
    "tags": [
      "群聊辅助",
      "综合脚本"
    ],
    "fileName": "7389201847563821056.zip",
    "fileState": 1,
    "createdAt": "2025-01-15T08:30:00.000Z",
    "images": {
      "id": 1,
      "cloudId": "7389201847563821056",
      "iconStatus": 1,
      "iconFilename": "icon.png",
      "previewStatus": 1,
      "previewFilename": [
        "1.png",
        "2.png",
        "3.png"
      ],
      "createdAt": "2025-01-15T08:30:00.000Z"
    },
    "audit": {
      "id": 10,
      "cloudId": "7389201847563821056",
      "status": 1,
      "reason": "功能完善，代码规范，通过审核",
      "processor": "admin",
      "uploaderUin": "1234567890",
      "uploaderNickname": "张三",
      "createdAt": "2025-01-15T08:30:00.000Z",
      "updatedAt": "2025-01-16T10:00:00.000Z"
    },
    "onlinePlugin": {
      "id": 5,
      "cloudId": "7389201847563821056",
      "pluginId": "com.example.groupmanager",
      "downloadCount": 1520,
      "reason": "首次上线",
      "status": 1,
      "processor": "admin",
      "updatedAt": "2025-01-16T10:00:00.000Z",
      "createdAt": "2025-01-16T10:00:00.000Z"
    }
  },
  "timestamp": 1705312200000
}
```

#### 响应字段说明

`data` 对象由 `plugin_info` 表字段 + 三个嵌套对象组成：

**顶层字段（来自 `plugin_info` 表）：**

| 字段            | 类型         | 说明                                                  |
|---------------|------------|-----------------------------------------------------|
| `cloudId`     | `string`   | 脚本云端唯一 ID（bigint → 字符串）                             |
| `pluginId`    | `string`   | 插件标识符（如 `com.example.xxx`）                          |
| `name`        | `string`   | 脚本名称                                                |
| `description` | `string`   | 脚本描述                                                |
| `author`      | `string`   | 作者名称                                                |
| `version`     | `string`   | 版本号（如 `1.0.0`）                                      |
| `type`        | `integer`  | 脚本类型编号                                              |
| `date`        | `string`   | 脚本日期标识（12 位字符，如 `"2025-01-15"`）                     |
| `tags`        | `string[]` | 分类标签数组                                              |
| `fileName`    | `string`   | 服务器内部 ZIP 文件名（如 `"7389201847563821056.zip"`），仅供内部使用 |
| `fileState`   | `integer`  | 文件存储状态：`0`=已删除，`1`=正常，`2`=过期                        |
| `createdAt`   | `string`   | 记录创建时间（ISO 8601）                                    |

**嵌套对象 `images`（来自 `plugin_images` 表，可为 `null`）：**

| 字段                | 类型                  | 说明                                                                                                          |
|-------------------|---------------------|-------------------------------------------------------------------------------------------------------------|
| `id`              | `integer`           | 图片记录 ID                                                                                                     |
| `cloudId`         | `string`            | 关联脚本 ID                                                                                                     |
| `iconStatus`      | `integer`           | 图标状态：`0`=无图标或提取失败，`1`=图标可用                                                                                  |
| `iconFilename`    | `string` / `null`   | 图标文件名（如 `"icon.png"`）。为 `null` 时无图标。通过 `GET /api/plugin/images/{cloudId}/{iconFilename}` 访问                 |
| `previewStatus`   | `integer`           | 预览图状态：`0`=无预览图或提取失败，`1`=预览图可用                                                                               |
| `previewFilename` | `string[]` / `null` | 预览图文件名数组（如 `["1.png", "2.png"]`），按数字排序。为 `null` 时无预览图。通过 `GET /api/plugin/images/{cloudId}/{filename}` 逐个访问 |
| `createdAt`       | `string`            | 记录创建时间（ISO 8601）                                                                                            |

**嵌套对象 `audit`（来自 `plugin_audit` 表，为最新一条记录，可为 `null`）：**

| 字段                 | 类型        | 说明                                     |
|--------------------|-----------|----------------------------------------|
| `id`               | `integer` | 审核记录自增 ID                              |
| `cloudId`          | `string`  | 关联脚本 ID                                |
| `status`           | `integer` | 审核状态：`0`=待审核，`1`=通过，`2`=未通过            |
| `reason`           | `string`  | 审核意见。通过时为正面评价，拒绝时为拒绝原因，待审核时可能为空字符串     |
| `processor`        | `string`  | 处理审核的管理员用户名。待审核时通常为空字符串                |
| `uploaderUin`      | `string`  | 上传者 QQ 号（10 位数字字符串，如 `"1234567890"`）   |
| `uploaderNickname` | `string`  | 上传者 QQ 昵称                              |
| `createdAt`        | `string`  | 记录创建时间（即上传时间或审核操作时间）                   |
| `updatedAt`        | `string`  | 记录更新时间（即审核处理时间）。与 `createdAt` 相同表示尚未处理 |

**嵌套对象 `onlinePlugin`（来自 `online_plugin` 表，可为 `null`）：**

| 字段              | 类型        | 说明                                      |
|-----------------|-----------|-----------------------------------------|
| `id`            | `integer` | 记录自增 ID                                 |
| `cloudId`       | `string`  | 关联脚本 ID                                 |
| `pluginId`      | `string`  | 插件标识符（与 `plugin_info` 中的 `pluginId` 一致） |
| `downloadCount` | `integer` | 累计下载次数，每次通过下载接口下载时 +1                   |
| `reason`        | `string`  | 上线/下架原因备注                               |
| `status`        | `integer` | 在线状态：`0`=已下架，`1`=在线                     |
| `processor`     | `string`  | 执行上线/下架操作的管理员用户名                        |
| `updatedAt`     | `string`  | 最后更新时间（ISO 8601）                        |
| `createdAt`     | `string`  | 上线时间（ISO 8601）                          |

---

### 4. GET `/api/plugin/public/scripts/:cloudId/audit` — 审核历史

查询指定脚本的完整审核历史记录，按 ID 倒序排列（最新记录在前）。一个脚本可能有多次审核记录（例如首次提交被拒绝后再次提交）。

#### 路径参数

| 参数        | 类型       | 必填 | 说明      |
|-----------|----------|----|---------|
| `cloudId` | `string` | 是  | 脚本云端 ID |

#### 响应示例

```json
{
  "status": 200,
  "message": "success",
  "data": [
    {
      "id": 10,
      "cloudId": "7389201847563821056",
      "status": 1,
      "reason": "功能完善，代码规范，通过审核",
      "processor": "admin",
      "uploaderUin": "1234567890",
      "uploaderNickname": "张三",
      "createdAt": "2025-01-15T08:30:00.000Z",
      "updatedAt": "2025-01-16T10:00:00.000Z"
    },
    {
      "id": 5,
      "cloudId": "7389201847563821056",
      "status": 2,
      "reason": "存在安全风险，请检查代码中的 eval 调用",
      "processor": "admin",
      "uploaderUin": "1234567890",
      "uploaderNickname": "张三",
      "createdAt": "2025-01-10T06:00:00.000Z",
      "updatedAt": "2025-01-12T09:00:00.000Z"
    }
  ],
  "timestamp": 1705312200000
}
```

#### 响应字段说明

`data` 为 `AuditRecord[]` 数组，每条记录的字段说明：

| 字段                 | 类型        | 说明                          |
|--------------------|-----------|-----------------------------|
| `id`               | `integer` | 审核记录自增 ID                   |
| `cloudId`          | `string`  | 关联脚本云端 ID                   |
| `status`           | `integer` | 审核状态：`0`=待审核，`1`=通过，`2`=未通过 |
| `reason`           | `string`  | 审核意见文本                      |
| `processor`        | `string`  | 处理审核的管理员用户名                 |
| `uploaderUin`      | `string`  | 上传者 QQ 号                    |
| `uploaderNickname` | `string`  | 上传者 QQ 昵称                   |
| `createdAt`        | `string`  | 记录创建时间（ISO 8601）            |
| `updatedAt`        | `string`  | 记录更新时间（ISO 8601）            |

---

### 5. GET `/api/plugin/public/scripts/:cloudId/ai-review` — AI 评审结果

查询指定脚本的 AI 自动评审结果。

**返回值可能性：**

| 场景           | `data` 值                                          | 说明                      |
|--------------|---------------------------------------------------|-------------------------|
| 脚本从未进行 AI 评审 | `null`                                            | `data` 直接为 `null`       |
| 评审正在进行中      | `{ reviewStatus: 0, reviewResult: null, ... }`    | `reviewResult` 为 `null` |
| 评审成功完成       | `{ reviewStatus: 1, reviewResult: { ... }, ... }` | `reviewResult` 包含完整分析   |
| 评审失败         | `{ reviewStatus: 2, errorMessage: "...", ... }`   | `errorMessage` 有值       |

#### 路径参数

| 参数        | 类型       | 必填 | 说明      |
|-----------|----------|----|---------|
| `cloudId` | `string` | 是  | 脚本云端 ID |

#### 响应示例（评审成功）

```json
{
  "status": 200,
  "message": "success",
  "data": {
    "id": 15,
    "cloudId": "7389201847563821056",
    "reviewStatus": 1,
    "reviewResult": {
      "compliance": {
        "passed": true,
        "issues": [
          {
            "level": "warning",
            "category": "代码质量",
            "message": "建议使用 const 替代 let 声明不会被重新赋值的变量",
            "location": "src/index.js:15"
          },
          {
            "level": "info",
            "category": "代码质量",
            "message": "函数 getUserInfo 可以提取为公共工具函数",
            "location": "src/utils.js:8"
          }
        ]
      },
      "summary": "脚本整体质量良好，代码结构清晰。存在少量代码质量建议，但不影响功能和安全性。",
      "suggestions": [
        "建议添加错误处理机制，避免未捕获的异常",
        "建议使用 async/await 替代回调函数，提升代码可读性"
      ],
      "riskLevel": "low"
    },
    "errorMessage": null,
    "modelUsed": "gpt-4",
    "tokensUsed": 2500,
    "createdAt": "2025-01-15T09:00:00.000Z",
    "updatedAt": "2025-01-15T09:05:00.000Z"
  },
  "timestamp": 1705312200000
}
```

#### 响应字段说明

**顶层字段：**

| 字段             | 类型                 | 说明                             |
|----------------|--------------------|--------------------------------|
| `id`           | `integer`          | 评审记录自增 ID                      |
| `cloudId`      | `string`           | 关联脚本云端 ID                      |
| `reviewStatus` | `integer`          | 评审状态：`0`=评审中，`1`=评审成功，`2`=评审失败 |
| `reviewResult` | `object` / `null`  | 评审结果详情，仅 `reviewStatus=1` 时有值  |
| `errorMessage` | `string` / `null`  | 错误信息，仅 `reviewStatus=2` 时有值    |
| `modelUsed`    | `string` / `null`  | 使用的 AI 模型名称                    |
| `tokensUsed`   | `integer` / `null` | 消耗的 token 数量                   |
| `createdAt`    | `string`           | 评审记录创建时间（ISO 8601）             |
| `updatedAt`    | `string`           | 评审完成/失败时间（ISO 8601）            |

**`reviewResult` 嵌套对象（仅评审成功时有值）：**

| 字段                  | 类型                  | 说明                                            |
|---------------------|---------------------|-----------------------------------------------|
| `compliance`        | `object`            | 合规性检查结果                                       |
| `compliance.passed` | `boolean`           | 是否通过合规性检查。`true`=无严重问题，`false`=存在 error 级别问题  |
| `compliance.issues` | `array`             | 发现的问题列表                                       |
| `summary`           | `string` / `null`   | AI 生成的评审摘要（自然语言描述）                            |
| `suggestions`       | `string[]` / `null` | 改进建议列表                                        |
| `riskLevel`         | `string`            | 风险等级：`"low"`=低风险，`"medium"`=中等风险，`"high"`=高风险 |

**`compliance.issues[]` 数组中的每个问题对象：**

| 字段         | 类型                | 说明                                           |
|------------|-------------------|----------------------------------------------|
| `level`    | `string`          | 严重级别：`"error"`=严重，`"warning"`=警告，`"info"`=提示 |
| `category` | `string`          | 问题分类（如 `"代码质量"`、`"安全性"`、`"性能"`、`"合规性"`）      |
| `message`  | `string`          | 问题的详细描述                                      |
| `location` | `string` / `null` | 问题所在代码位置（如 `"src/index.js:15"`），可能为 `null`   |

---

### 6. GET `/api/plugin/public/statistics` — 平台统计信息

获取平台的全面统计数据，包括脚本、审核、下载、AI 评审、评论、用户、标签分布和近期活动等指标。无需任何参数。

#### 请求参数

无。

#### 响应示例

```json
{
  "status": 200,
  "message": "success",
  "data": {
    "scripts": {
      "total": 85,
      "online": 42,
      "offline": 10,
      "notPublished": 33
    },
    "audits": {
      "total": 120,
      "pending": 8,
      "approved": 90,
      "rejected": 22
    },
    "downloads": {
      "total": 15230
    },
    "aiReviews": {
      "total": 80,
      "success": 70,
      "failed": 5,
      "totalTokensUsed": 185000,
      "riskDistribution": {
        "low": 50,
        "medium": 15,
        "high": 5
      }
    },
    "comments": {
      "total": 320,
      "active": 298
    },
    "users": {
      "uploaders": 45,
      "commenters": 120,
      "blacklisted": 3
    },
    "tags": {
      "群聊辅助": 25,
      "娱乐功能": 18,
      "功能扩展": 22,
      "综合脚本": 12,
      "官方脚本": 8
    },
    "recentActivity": {
      "todayUploads": 2,
      "weekUploads": 8,
      "monthUploads": 25
    }
  },
  "timestamp": 1705312200000
}
```

#### 响应字段说明

**`scripts` — 脚本统计：**

| 字段             | 类型        | 说明                                                           |
|----------------|-----------|--------------------------------------------------------------|
| `total`        | `integer` | 脚本总数                                                         |
| `online`       | `integer` | 当前在线脚本数（`online_plugin.status = 1`）                          |
| `offline`      | `integer` | 已下架脚本数（`online_plugin.status = 0`）                           |
| `notPublished` | `integer` | 未上线脚本数（无 `online_plugin` 记录）。计算方式：`total - online - offline` |

**`audits` — 审核统计：**

| 字段         | 类型        | 说明                   |
|------------|-----------|----------------------|
| `total`    | `integer` | 审核记录总数（含所有历史记录）      |
| `pending`  | `integer` | 当前待审核数（`status = 0`） |
| `approved` | `integer` | 已通过数（`status = 1`）   |
| `rejected` | `integer` | 已拒绝数（`status = 2`）   |

**`downloads` — 下载统计：**

| 字段      | 类型        | 说明        |
|---------|-----------|-----------|
| `total` | `integer` | 全平台累计总下载量 |

**`aiReviews` — AI 评审统计：**

| 字段                        | 类型        | 说明                        |
|---------------------------|-----------|---------------------------|
| `total`                   | `integer` | AI 评审记录总数                 |
| `success`                 | `integer` | 评审成功数（`reviewStatus = 1`） |
| `failed`                  | `integer` | 评审失败数（`reviewStatus = 2`） |
| `totalTokensUsed`         | `integer` | AI 评审累计消耗 token 数         |
| `riskDistribution`        | `object`  | 风险等级分布（仅统计评审成功的记录）        |
| `riskDistribution.low`    | `integer` | 低风险脚本数                    |
| `riskDistribution.medium` | `integer` | 中等风险脚本数                   |
| `riskDistribution.high`   | `integer` | 高风险脚本数                    |

**`comments` — 评论统计：**

| 字段       | 类型        | 说明                     |
|----------|-----------|------------------------|
| `total`  | `integer` | 评论总数（含所有状态）            |
| `active` | `integer` | 正常显示的评论数（`status = 1`） |

**`users` — 用户统计：**

| 字段            | 类型        | 说明        |
|---------------|-----------|-----------|
| `uploaders`   | `integer` | 去重后的上传者数量 |
| `commenters`  | `integer` | 去重后的评论者数量 |
| `blacklisted` | `integer` | 黑名单用户数    |

**`tags` — 标签分布：**

| 字段     | 类型       | 说明                                                                    |
|--------|----------|-----------------------------------------------------------------------|
| `tags` | `object` | 各标签对应的脚本数量，键为标签名称（如 `"群聊辅助"`），值为该标签下的脚本数。一个脚本可有多个标签，因此各标签数量之和可能大于脚本总数 |

**`recentActivity` — 近期活动：**

| 字段             | 类型        | 说明                  |
|----------------|-----------|---------------------|
| `todayUploads` | `integer` | 今日上传的脚本数            |
| `weekUploads`  | `integer` | 本周上传的脚本数（从本周一开始计算）  |
| `monthUploads` | `integer` | 本月上传的脚本数（从本月1日开始计算） |

---

### 7. GET `/api/plugin/public/scripts/:cloudId/download` — 下载脚本

下载指定脚本的 ZIP 文件。下载次数会自动 +1（异步执行，不影响响应速度）。

#### 路径参数

| 参数        | 类型       | 必填 | 说明      |
|-----------|----------|----|---------|
| `cloudId` | `string` | 是  | 脚本云端 ID |

#### 响应

**成功时**：直接返回 ZIP 文件二进制流（`Content-Type: application/octet-stream`）。

**响应 Headers：**

| Header                | 说明                                | 示例                                                                            |
|-----------------------|-----------------------------------|-------------------------------------------------------------------------------|
| `Content-Type`        | 固定为 `application/octet-stream`    | `application/octet-stream`                                                    |
| `Content-Disposition` | 下载文件名，格式为 UTF-8 编码的 `脚本名-版本号.zip` | `attachment; filename*=UTF-8''%E7%BE%A4%E7%AE%A1%E5%8A%A9%E6%89%8B-1.0.0.zip` |

> **容灾机制**：如果本地 ZIP 文件不存在，系统会自动从腾讯云 COS 下载到本地后再返回。这个过程对调用者透明，可能增加响应时间。

---

## 数据模型速查表

### PluginImages（图片信息）

| 字段                | 类型         | 可 null | 说明         |
|-------------------|------------|--------|------------|
| `id`              | `integer`  | 否      | 记录 ID      |
| `cloudId`         | `string`   | 否      | 关联脚本 ID    |
| `iconStatus`      | `integer`  | 否      | 图标状态（0/1）  |
| `iconFilename`    | `string`   | 是      | 图标文件名      |
| `previewStatus`   | `integer`  | 否      | 预览图状态（0/1） |
| `previewFilename` | `string[]` | 是      | 预览图文件名数组   |
| `createdAt`       | `string`   | 否      | 创建时间       |

**图片访问 URL**：`GET /api/plugin/images/{cloudId}/{filename}`

---

## 常见开发场景

### 场景 1：展示脚本列表

1. 调用 `GET /api/plugin/public/scripts?page=1&pageSize=20&sort=time`
2. 遍历 `data.list` 渲染列表项
3. 使用 `data.totalPages` 渲染分页控件
4. 图片 URL 拼接：`/api/plugin/images/${item.cloudId}/${item.images?.iconFilename}`
5. 根据枚举值映射显示审核状态标签

### 场景 2：搜索脚本

1. 用户输入关键词后调用 `GET /api/plugin/public/search?keyword=xxx`
2. 响应结构与列表接口相同，直接复用列表组件渲染

### 场景 3：查看脚本详情

1. 用户点击列表项，携带 `cloudId` 调用 `GET /api/plugin/public/scripts/{cloudId}`
2. 展示脚本基础信息（`name`、`description`、`author`、`version`、`tags`）
3. 展示审核信息（`audit.status`、`audit.reason`、`audit.processor`）
4. 展示在线信息和下载量（`onlinePlugin.downloadCount`）
5. 展示预览图（`images.previewFilename` 数组循环渲染）
6. 展示 AI 评审结果（需额外调用 AI 评审接口）

### 场景 4：下载脚本

1. 用户点击下载按钮，请求 `GET /api/plugin/public/scripts/{cloudId}/download`
2. 浏览器会自动触发文件下载（通过 `Content-Disposition` header 获取文件名）
3. 如果是前端 SPA，可以使用 `window.open(url)` 或 `<a>` 标签直接跳转

### 场景 5：展示平台统计仪表盘

1. 调用 `GET /api/plugin/public/statistics` 获取全量统计数据
2. 使用 `scripts` 渲染脚本总览卡片（总数、在线、下架、未上线）
3. 使用 `audits` 渲染审核进度（待审核数可用于提醒管理员）
4. 使用 `downloads.total` 展示累计下载量
5. 使用 `aiReviews.riskDistribution` 渲染风险等级饼图
6. 使用 `tags` 渲染标签分布柱状图
7. 使用 `recentActivity` 展示近期上传趋势
