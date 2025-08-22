# Sentry 查询语句速查表

> 常用的 Sentry 查询语句参考，方便快速筛选和定位问题

---

## 基础筛选语法

### 环境和版本
```bash
# 按环境筛选
environment:production
environment:test
environment:development

# 按版本筛选
release:"my-app@1.2.0"
release:"my-app@*"  # 所有版本

# 版本范围（需要语义化版本）
release:">my-app@1.0.0"
release:"<my-app@2.0.0"
```

### 时间范围
```bash
# 绝对时间
timestamp:>2024-01-01
timestamp:<2024-01-31
timestamp:2024-01-15

# 相对时间
first_seen:>-24h    # 24小时内首次出现
last_seen:<-7d      # 7天前最后出现
first_seen:>2024-01-15  # 指定日期后首次出现
```

### 错误状态
```bash
# 错误状态
is:unresolved       # 未解决
is:resolved         # 已解决
is:ignored          # 已忽略
is:assigned         # 已分配

# 错误级别
level:error
level:warning
level:info
level:debug
```

---

## 用户和设备筛选

### 用户信息
```bash
# 用户ID
user.id:"12345"
user.id:"user_*"    # 通配符匹配

# 用户邮箱
user.email:"user@example.com"
user.email:"*@company.com"  # 域名匹配

# 用户名
user.username:"john_doe"

# IP地址
user.ip_address:"192.168.1.100"
user.ip_address:"192.168.*"  # IP段匹配
```

### 设备和浏览器
```bash
# 浏览器
browser.name:"Chrome"
browser.name:"Safari"
browser.name:"Firefox"
browser.name:"Mobile Safari"

# 浏览器版本
browser.version:"91.0.4472.124"
browser.version:"91.*"  # 版本匹配

# 操作系统
os.name:"Windows"
os.name:"macOS"
os.name:"iOS"
os.name:"Android"

# 设备类型
device.family:"iPhone"
device.family:"iPad"
device.family:"Samsung"
```

---

## 错误类型和消息

### 错误类型
```bash
# JavaScript 错误类型
error.type:"TypeError"
error.type:"ReferenceError"
error.type:"SyntaxError"
error.type:"RangeError"

# HTTP 错误
error.type:"ChunkLoadError"  # 代码分割加载错误
error.type:"NetworkError"    # 网络错误
```

### 错误消息
```bash
# 消息内容匹配
message:"Cannot read property"
message:"*undefined*"        # 包含 undefined
message:"*timeout*"          # 包含 timeout
message:"*404*"              # 包含 404

# 正则表达式匹配
message:/Loading chunk \d+ failed/
message:/Cannot read property '\w+' of undefined/
```

---

## URL 和路径筛选

### 页面URL
```bash
# 精确匹配
url:"https://example.com/checkout"

# 路径匹配
url:"*/checkout/*"           # 包含 checkout 路径
url:"*/api/*"                # API 相关页面
url:"*/admin/*"              # 管理后台页面

# 域名匹配
url:"https://example.com/*"  # 特定域名
url:"https://*.example.com/*" # 子域名匹配
```

### 文件路径
```bash
# 源码文件
stack.filename:"*/components/Payment.js"
stack.filename:"*/utils/*"   # 工具函数相关
stack.filename:"*/api/*"     # API 相关文件

# 第三方库
stack.filename:"*/node_modules/*"
stack.filename:"*/vendor/*"
```

---

## 自定义标签筛选

### 业务模块
```bash
# 功能模块
tag:module:"payment"         # 支付模块
tag:module:"user"            # 用户模块
tag:module:"order"           # 订单模块
tag:module:"product"         # 商品模块

# 页面类型
tag:page_type:"checkout"     # 结账页面
tag:page_type:"profile"      # 个人资料页
tag:page_type:"dashboard"    # 仪表板
```

### 技术标签
```bash
# 平台类型
tag:platform:"web"          # Web 平台
tag:platform:"mobile"       # 移动端
tag:platform:"desktop"      # 桌面端

# 框架版本
tag:framework:"vue3"        # Vue 3
tag:framework:"react"       # React
tag:framework:"angular"     # Angular

# 构建环境
tag:build:"webpack"         # Webpack 构建
tag:build:"vite"            # Vite 构建
```

---

## 组合查询示例

### 生产环境问题排查
```bash
# 生产环境的支付相关错误
environment:production AND tag:module:"payment"

# 生产环境新出现的错误
environment:production AND first_seen:>-24h

# 生产环境高频错误
environment:production AND times_seen:>100
```

### 用户体验问题
```bash
# 移动端用户的错误
tag:platform:"mobile" AND (os.name:"iOS" OR os.name:"Android")

# 特定浏览器的兼容性问题
browser.name:"Safari" AND error.type:"TypeError"

# 网络相关错误
message:"*network*" OR message:"*timeout*" OR message:"*failed to fetch*"
```

### 版本发布监控
```bash
# 新版本的错误
release:"my-app@2.0.0" AND first_seen:>2024-01-15

# 版本对比（需要在界面中操作）
release:"my-app@2.0.0" OR release:"my-app@1.9.0"

# 新版本的性能问题
release:"my-app@2.0.0" AND tag:type:"performance"
```

### 特定功能监控
```bash
# 登录相关问题
tag:module:"auth" OR url:"*/login/*" OR url:"*/register/*"

# 支付流程问题
tag:module:"payment" OR url:"*/checkout/*" OR url:"*/payment/*"

# API 接口问题
tag:type:"api" OR message:"*fetch*" OR message:"*xhr*"
```

---

## 排除查询

### 排除无关错误
```bash
# 排除第三方脚本错误
!stack.filename:"*/node_modules/*"

# 排除特定浏览器
!browser.name:"Internet Explorer"

# 排除测试环境
!environment:test

# 排除已知的无害错误
!message:"ResizeObserver loop limit exceeded"
!message:"Script error."
!message:"Non-Error promise rejection captured"
```

### 排除特定用户
```bash
# 排除内部测试用户
!user.email:"*@company.com"
!user.id:"test_*"

# 排除爬虫和机器人
!user.ip_address:"127.0.0.1"
!browser.name:"*bot*"
```

---

## 性能相关查询

### 页面性能
```bash
# 慢页面加载
transaction.duration:>3000   # 超过3秒

# 特定页面性能
transaction:"/checkout" AND transaction.duration:>2000

# 移动端性能问题
tag:platform:"mobile" AND transaction.duration:>5000
```

### 接口性能
```bash
# 慢接口
transaction.op:"http.client" AND transaction.duration:>1000

# 特定接口性能
transaction:"/api/payment" AND transaction.duration:>500

# 接口错误
transaction.op:"http.client" AND transaction.status:"error"
```

---

## 常用保存查询建议

### 日常监控查询
```bash
# 1. 生产环境未解决错误
名称："生产环境待处理错误"
查询：environment:production AND is:unresolved

# 2. 高影响错误
名称："高影响用户错误"
查询：environment:production AND user.count:>10

# 3. 新错误监控
名称："24小时内新错误"
查询：environment:production AND first_seen:>-24h

# 4. 支付模块监控
名称："支付模块错误"
查询：tag:module:"payment" AND environment:production
```

### 专项分析查询
```bash
# 5. 移动端问题
名称："移动端用户问题"
查询：tag:platform:"mobile" AND environment:production

# 6. 接口异常监控
名称："接口调用异常"
查询：tag:type:"api" AND environment:production

# 7. 第三方集成问题
名称："第三方SDK问题"
查询：tag:type:"third_party" AND environment:production

# 8. 性能问题监控
名称："页面性能问题"
查询：transaction.duration:>3000 AND environment:production
```

---

## 查询优化建议

### 提高查询效率
1. **使用精确的时间范围**
   ```bash
   # 好：指定具体时间范围
   timestamp:>2024-01-01 AND timestamp:<2024-01-31
   
   # 避免：过大的时间范围
   timestamp:>2023-01-01
   ```

2. **优先使用索引字段**
   ```bash
   # 高效：使用索引字段
   environment:production
   release:"my-app@1.0.0"
   
   # 低效：复杂的消息匹配
   message:"*very specific long error message*"
   ```

3. **合理使用通配符**
   ```bash
   # 好：前缀匹配
   user.id:"user_*"
   
   # 避免：中间通配符
   user.id:"*_user_*"
   ```

### 查询最佳实践
1. **从宽到窄逐步筛选**
2. **保存常用查询避免重复输入**
3. **使用标签系统进行分类**
4. **定期清理和优化保存的查询**
5. **团队共享重要的查询语句**

---

## 快速参考卡片

### 基础语法
| 操作符 | 说明 | 示例 |
|--------|------|------|
| `:` | 等于 | `environment:production` |
| `!` | 不等于 | `!browser.name:"Chrome"` |
| `*` | 通配符 | `user.email:"*@company.com"` |
| `>` | 大于 | `timestamp:>2024-01-01` |
| `<` | 小于 | `transaction.duration:<1000` |
| `AND` | 逻辑与 | `environment:production AND level:error` |
| `OR` | 逻辑或 | `browser.name:"Chrome" OR browser.name:"Safari"` |
| `()` | 分组 | `(environment:production OR environment:staging) AND level:error` |

### 常用字段
| 字段类型 | 字段名 | 说明 |
|----------|--------|------|
| 环境 | `environment` | 运行环境 |
| 版本 | `release` | 应用版本 |
| 用户 | `user.id`, `user.email` | 用户信息 |
| 设备 | `browser.name`, `os.name` | 设备信息 |
| 错误 | `error.type`, `message` | 错误信息 |
| 页面 | `url`, `transaction` | 页面信息 |
| 标签 | `tag:key:"value"` | 自定义标签 |
| 时间 | `timestamp`, `first_seen` | 时间信息 |