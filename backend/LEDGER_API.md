# Ledger App API 文档

## 概述

Ledger App 是一个账本管理系统，提供用户认证、账本管理和交易记录功能。

**基础URL**: `http://localhost:8000/api`  
**API文档**: `http://localhost:8000/docs` (Swagger UI)

## 认证

大部分API端点需要JWT认证。在请求头中添加：
```
Authorization: Bearer {access_token}
```

Token通过登录接口获取，有效期为30分钟。

---

## 1. 用户认证 API

### 1.1 用户注册

**POST** `/api/register`

注册新用户。

**请求体** (JSON):
```json
{
  "username": "string",
  "password": "string",
  "repeat_password": "string"
}
```

**响应**: `200 OK`
```json
{
  "detail": "User registered successfully"
}
```

**错误响应**:
- `400 Bad Request`: 密码不匹配
- `409 Conflict`: 用户名已存在

---

### 1.2 用户登录

**POST** `/api/login`

用户登录，获取访问令牌。

**请求体** (Form Data - `application/x-www-form-urlencoded`):
```
username=string&password=string
```

**响应**: `200 OK`
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer"
}
```

**错误响应**:
- `401 Unauthorized`: 用户名或密码错误

**使用示例**:
```bash
curl -X POST "http://localhost:8000/api/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=WJL&password=your_password"
```

---

## 2. 账本 (Ledger) API

### 2.1 创建账本

**POST** `/api/ledgers`

创建新的账本。

**请求头**:
```
Authorization: Bearer {token}
```

**请求体** (JSON):
```json
{
  "name": "个人账本",
  "description": "用于记录个人收支"
}
```

**响应**: `201 Created`
```json
{
  "id": 1,
  "user_id": 1,
  "name": "个人账本",
  "description": "用于记录个人收支",
  "created_at": "2025-12-07T15:00:00",
  "updated_at": null
}
```

---

### 2.2 获取账本列表

**GET** `/api/ledgers?skip=0&limit=100`

获取当前用户的所有账本列表。

**请求头**:
```
Authorization: Bearer {token}
```

**查询参数**:
- `skip` (int, 可选): 跳过的记录数，默认0
- `limit` (int, 可选): 返回的记录数，默认100

**响应**: `200 OK`
```json
{
  "total": 2,
  "items": [
    {
      "id": 1,
      "user_id": 1,
      "name": "个人账本",
      "description": "用于记录个人收支",
      "created_at": "2025-12-07T15:00:00",
      "updated_at": null
    },
    {
      "id": 2,
      "user_id": 1,
      "name": "工作账本",
      "description": "工作相关收支",
      "created_at": "2025-12-07T16:00:00",
      "updated_at": null
    }
  ]
}
```

---

### 2.3 获取单个账本

**GET** `/api/ledgers/{ledger_id}`

获取指定ID的账本详情。

**请求头**:
```
Authorization: Bearer {token}
```

**路径参数**:
- `ledger_id` (int): 账本ID

**响应**: `200 OK`
```json
{
  "id": 1,
  "user_id": 1,
  "name": "个人账本",
  "description": "用于记录个人收支",
  "created_at": "2025-12-07T15:00:00",
  "updated_at": null
}
```

**错误响应**:
- `404 Not Found`: 账本不存在

---

### 2.4 更新账本

**PUT** `/api/ledgers/{ledger_id}`

更新账本信息。

**请求头**:
```
Authorization: Bearer {token}
```

**路径参数**:
- `ledger_id` (int): 账本ID

**请求体** (JSON):
```json
{
  "name": "更新后的账本名称",
  "description": "更新后的描述"
}
```

**响应**: `200 OK`
```json
{
  "id": 1,
  "user_id": 1,
  "name": "更新后的账本名称",
  "description": "更新后的描述",
  "created_at": "2025-12-07T15:00:00",
  "updated_at": "2025-12-07T17:00:00"
}
```

---

### 2.5 删除账本

**DELETE** `/api/ledgers/{ledger_id}`

删除指定的账本。

**请求头**:
```
Authorization: Bearer {token}
```

**路径参数**:
- `ledger_id` (int): 账本ID

**响应**: `204 No Content`

**错误响应**:
- `404 Not Found`: 账本不存在

---

## 3. 交易 (Transaction) API

### 3.1 创建交易记录

**POST** `/api/transactions`

创建新的交易记录。

**请求头**:
```
Authorization: Bearer {token}
```

**请求体** (JSON):
```json
{
  "type": "expense",
  "amount": 100.50,
  "category": "餐饮",
  "description": "午餐",
  "date": "2025-12-07T12:00:00"
}
```

**字段说明**:
- `type` (string, 必填): 交易类型，必须是 `"income"` 或 `"expense"`
- `amount` (float, 必填): 金额，必须大于0
- `category` (string, 必填): 分类，如 "餐饮"、"交通"、"工资" 等
- `description` (string, 可选): 描述
- `date` (datetime, 可选): 交易日期，默认为当前时间

**响应**: `201 Created`
```json
{
  "id": 1,
  "user_id": 1,
  "type": "expense",
  "amount": 100.50,
  "category": "餐饮",
  "description": "午餐",
  "date": "2025-12-07T12:00:00",
  "created_at": "2025-12-07T12:00:00",
  "updated_at": null
}
```

---

### 3.2 获取交易记录列表

**GET** `/api/transactions?skip=0&limit=100&type=expense&category=餐饮&start_date=2025-12-01&end_date=2025-12-31`

获取交易记录列表，支持多种筛选条件。

**请求头**:
```
Authorization: Bearer {token}
```

**查询参数**:
- `skip` (int, 可选): 跳过的记录数，默认0，最小值0
- `limit` (int, 可选): 返回的记录数，默认100，范围1-1000
- `type` (string, 可选): 交易类型筛选，`"income"` 或 `"expense"`
- `category` (string, 可选): 分类筛选
- `start_date` (datetime, 可选): 开始日期，格式: `2025-12-01T00:00:00`
- `end_date` (datetime, 可选): 结束日期，格式: `2025-12-31T23:59:59`

**响应**: `200 OK`
```json
{
  "total": 10,
  "items": [
    {
      "id": 1,
      "user_id": 1,
      "type": "expense",
      "amount": 100.50,
      "category": "餐饮",
      "description": "午餐",
      "date": "2025-12-07T12:00:00",
      "created_at": "2025-12-07T12:00:00",
      "updated_at": null
    }
  ]
}
```

---

### 3.3 获取单个交易记录

**GET** `/api/transactions/{transaction_id}`

获取指定ID的交易记录详情。

**请求头**:
```
Authorization: Bearer {token}
```

**路径参数**:
- `transaction_id` (int): 交易记录ID

**响应**: `200 OK`
```json
{
  "id": 1,
  "user_id": 1,
  "type": "expense",
  "amount": 100.50,
  "category": "餐饮",
  "description": "午餐",
  "date": "2025-12-07T12:00:00",
  "created_at": "2025-12-07T12:00:00",
  "updated_at": null
}
```

**错误响应**:
- `404 Not Found`: 交易记录不存在

---

### 3.4 更新交易记录

**PUT** `/api/transactions/{transaction_id}`

更新交易记录信息。

**请求头**:
```
Authorization: Bearer {token}
```

**路径参数**:
- `transaction_id` (int): 交易记录ID

**请求体** (JSON):
```json
{
  "type": "expense",
  "amount": 150.00,
  "category": "交通",
  "description": "出租车费",
  "date": "2025-12-07T14:00:00"
}
```

**注意**: 所有字段都是可选的，只传递需要更新的字段。

**响应**: `200 OK`
```json
{
  "id": 1,
  "user_id": 1,
  "type": "expense",
  "amount": 150.00,
  "category": "交通",
  "description": "出租车费",
  "date": "2025-12-07T14:00:00",
  "created_at": "2025-12-07T12:00:00",
  "updated_at": "2025-12-07T15:00:00"
}
```

---

### 3.5 删除交易记录

**DELETE** `/api/transactions/{transaction_id}`

删除指定的交易记录。

**请求头**:
```
Authorization: Bearer {token}
```

**路径参数**:
- `transaction_id` (int): 交易记录ID

**响应**: `204 No Content`

**错误响应**:
- `404 Not Found`: 交易记录不存在

---

### 3.6 获取交易统计摘要

**GET** `/api/transactions/summary/statistics?start_date=2025-12-01&end_date=2025-12-31`

获取交易统计摘要（总收入、总支出、余额）。

**请求头**:
```
Authorization: Bearer {token}
```

**查询参数**:
- `start_date` (datetime, 可选): 开始日期
- `end_date` (datetime, 可选): 结束日期

**响应**: `200 OK`
```json
{
  "total_income": 5000.00,
  "total_expense": 3200.50,
  "balance": 1799.50
}
```

---

## 数据模型

### RegisterRequest
```json
{
  "username": "string",
  "password": "string",
  "repeat_password": "string"
}
```

### LoginResponse
```json
{
  "access_token": "string",
  "token_type": "bearer"
}
```

### LedgerCreate
```json
{
  "name": "string",          // 必填，1-100字符
  "description": "string"    // 可选，最多500字符
}
```

### LedgerUpdate
```json
{
  "name": "string",          // 可选，1-100字符
  "description": "string"    // 可选，最多500字符
}
```

### LedgerResponse
```json
{
  "id": 1,
  "user_id": 1,
  "name": "string",
  "description": "string | null",
  "created_at": "datetime",
  "updated_at": "datetime | null"
}
```

### TransactionCreate
```json
{
  "type": "income | expense",  // 必填
  "amount": 100.50,            // 必填，必须大于0
  "category": "string",        // 必填
  "description": "string",    // 可选
  "date": "datetime"           // 可选，默认为当前时间
}
```

### TransactionUpdate
```json
{
  "type": "income | expense",  // 可选
  "amount": 100.50,            // 可选，必须大于0
  "category": "string",        // 可选
  "description": "string",     // 可选
  "date": "datetime"           // 可选
}
```

### TransactionResponse
```json
{
  "id": 1,
  "user_id": 1,
  "type": "income | expense",
  "amount": 100.50,
  "category": "string",
  "description": "string | null",
  "date": "datetime",
  "created_at": "datetime",
  "updated_at": "datetime | null"
}
```

### TransactionListResponse
```json
{
  "total": 10,
  "items": [TransactionResponse, ...]
}
```

### TransactionSummaryResponse
```json
{
  "total_income": 5000.00,
  "total_expense": 3200.50,
  "balance": 1799.50
}
```

---

## 使用示例

### 完整流程示例

```bash
# 1. 登录获取token
TOKEN=$(curl -X POST "http://localhost:8000/api/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=WJL&password=your_password" | jq -r '.access_token')

# 2. 创建账本
curl -X POST "http://localhost:8000/api/ledgers" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "个人账本",
    "description": "用于记录个人收支"
  }'

# 3. 获取账本列表
curl -X GET "http://localhost:8000/api/ledgers" \
  -H "Authorization: Bearer $TOKEN"

# 4. 创建交易记录
curl -X POST "http://localhost:8000/api/transactions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "expense",
    "amount": 100.50,
    "category": "餐饮",
    "description": "午餐",
    "date": "2025-12-07T12:00:00"
  }'

# 5. 获取交易列表
curl -X GET "http://localhost:8000/api/transactions?type=expense&limit=10" \
  -H "Authorization: Bearer $TOKEN"

# 6. 获取交易统计
curl -X GET "http://localhost:8000/api/transactions/summary/statistics" \
  -H "Authorization: Bearer $TOKEN"

# 7. 更新交易记录
curl -X PUT "http://localhost:8000/api/transactions/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 150.00,
    "category": "交通"
  }'

# 8. 删除交易记录
curl -X DELETE "http://localhost:8000/api/transactions/1" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 错误响应

所有API在出错时都会返回标准的错误响应：

```json
{
  "detail": "错误描述信息"
}
```

### 常见HTTP状态码

- `200 OK`: 请求成功
- `201 Created`: 资源创建成功
- `204 No Content`: 删除成功，无返回内容
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 未授权，需要登录或token无效
- `404 Not Found`: 资源不存在
- `409 Conflict`: 资源冲突（如用户名已存在）
- `500 Internal Server Error`: 服务器内部错误

---

## 注意事项

1. **认证**: 除登录和注册接口外，所有API都需要在请求头中携带有效的JWT token
2. **权限**: 用户只能访问和操作自己的数据（账本和交易记录）
3. **日期格式**: 所有日期时间字段使用ISO 8601格式: `YYYY-MM-DDTHH:MM:SS`
4. **分页**: 列表接口支持分页，使用 `skip` 和 `limit` 参数
5. **筛选**: 交易记录列表支持按类型、分类、日期范围筛选
6. **数据验证**: 
   - 交易金额必须大于0
   - 交易类型必须是 `"income"` 或 `"expense"`
   - 账本名称长度1-100字符
   - 账本描述最多500字符

---

## API端点总览

### 用户认证
- `POST /api/register` - 用户注册
- `POST /api/login` - 用户登录

### 账本管理
- `POST /api/ledgers` - 创建账本
- `GET /api/ledgers` - 获取账本列表
- `GET /api/ledgers/{ledger_id}` - 获取单个账本
- `PUT /api/ledgers/{ledger_id}` - 更新账本
- `DELETE /api/ledgers/{ledger_id}` - 删除账本

### 交易记录
- `POST /api/transactions` - 创建交易记录
- `GET /api/transactions` - 获取交易列表
- `GET /api/transactions/{transaction_id}` - 获取单个交易记录
- `PUT /api/transactions/{transaction_id}` - 更新交易记录
- `DELETE /api/transactions/{transaction_id}` - 删除交易记录
- `GET /api/transactions/summary/statistics` - 获取交易统计

---

## 交互式API文档

启动服务器后，可以访问以下地址查看交互式API文档：

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

这些文档提供了完整的API说明、请求/响应示例，并支持直接在浏览器中测试API。
