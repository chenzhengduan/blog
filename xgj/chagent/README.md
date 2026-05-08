# Xzagent Docker 部署

本文档说明如何仅基于 `jegent` 后端项目构建和运行 Docker 镜像。

## 前提

- Docker 已安装
- 目标数据库与外部依赖可从容器网络访问

## 构建镜像

在本目录执行：

```bash
docker build -t xzagent:latest .
```

## 运行镜像

最小运行示例：

```bash
docker run --name xzagent \
  -p 8080:8080 \
  -e XZAGENT_DB_URL='jdbc:mysql://your-mysql-host:3306/jegent_dev?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8' \
  -e XZAGENT_DB_USERNAME='your_db_user' \
  -e XZAGENT_DB_PASSWORD='your_db_password' \
  -e ANTHROPIC_API_KEY='your_anthropic_api_key' \
  -e XZAGENT_JWT_SECRET='replace-with-a-strong-secret' \
  -e XZAGENT_HEADER_ENCRYPTION_PASSWORD='replace-with-a-strong-password' \
  -e XZAGENT_HEADER_ENCRYPTION_SALT='replace-with-16-hex-chars' \
  xzagent:latest
```

## 常用环境变量

### 必填

- `XZAGENT_DB_URL`
- `XZAGENT_DB_USERNAME`
- `XZAGENT_DB_PASSWORD`
- `XZAGENT_JWT_SECRET`
- `XZAGENT_HEADER_ENCRYPTION_PASSWORD`
- `XZAGENT_HEADER_ENCRYPTION_SALT`

### 模型密钥

- 使用默认 `anthropic` 时，需要 `ANTHROPIC_API_KEY`
- 设置 `XZAGENT_MODEL_PROVIDER=openai` 时，需要 `OPENAI_API_KEY`

### 常用可选项

- `SERVER_PORT`
  - Spring Boot 端口，默认 `8080`
- `XZAGENT_MODEL_PROVIDER`
  - 模型服务商，支持 `anthropic`、`openai`，默认 `anthropic`
- `XZAGENT_ANTHROPIC_MODEL`
  - 默认 `claude-sonnet-4-5-20250929`
- `XZAGENT_ANTHROPIC_BASE_URL`
  - 如果走代理或私有网关，可配置
- `XZAGENT_OPENAI_MODEL`
  - OpenAI 模型名称，默认 `gpt-4o-mini`
- `XZAGENT_OPENAI_BASE_URL`
  - OpenAI API 兼容服务地址；可用于代理、私有网关、DeepSeek 或 vLLM 等兼容服务
- `XZAGENT_REMOTE_TOOL_LOG_RAW_PAYLOADS`
  - 是否打印远程工具原始入参与出参，默认 `false`
- `XZAGENT_REMOTE_TOOL_MAX_PAYLOAD_LENGTH`
  - 原始日志最大长度，默认 `4000`
- `XZAGENT_A2A_ENABLED`
  - 是否启用 A2A 服务，默认 `true`
- `XZAGENT_A2A_HOST`
  - A2A AgentCard 中返回的主机名，默认 `127.0.0.1`
- `XZAGENT_A2A_PORT`
  - A2A AgentCard 中返回的端口，默认跟随后端端口

## Playwright 截图工具说明

当前仓库提供两种镜像构建方式：

1. 默认 Playwright 运行时镜像
```bash
docker build -t xzagent:latest .
```

2. 轻量兼容镜像
```bash
docker build --target runtime-slim -t xzagent:slim .
```

默认镜像已经适合在容器里执行 HTML 截图。

默认情况下，截图工具**启用**。如需显式关闭，请设置：

```bash
-e XZAGENT_HTML_SCREENSHOT_ENABLED=false
```

工具开启后，生成的 PNG 会落到容器内：

```text
/tmp/xzagent-html-screenshots
```

同时可以通过应用内路径访问：

```text
/api/tools/html-screenshots/<fileName>
```

由于默认镜像已经包含 Playwright 运行时，如果你要稳定使用截图工具，建议同时：

1. 使用默认镜像 `xzagent:latest`
2. 运行容器时附带：

```bash
docker run --init --ipc=host ...
```

原因：
- `--init` 有助于避免浏览器子进程僵尸进程问题
- `--ipc=host` 更利于 Chromium 在容器内稳定运行

## A2A 相关说明

当前镜像启动后，A2A 服务与后端使用同一个端口。

可用入口：

- `GET /.well-known/agent-card.json`
- `POST /`

如果启用了 A2A API Key 鉴权，请在调用 `POST /` 时携带：

- `X-API-Key: sk-...`

或：

- `Authorization: Bearer sk-...`

## 健康检查

服务启动后可访问：

```bash
curl http://localhost:8080/actuator/health
```

返回 `{"status":"UP"}` 表示后端已启动。

## 生产建议

- 不要使用默认的数据库地址、JWT 密钥和加密密码
- 容器外部通过环境变量或密钥管理系统注入敏感信息
- `XZAGENT_A2A_HOST` 应配置为真实可访问域名或网关域名
- 如果远程来源会打印原始请求日志，生产环境保持
  `XZAGENT_REMOTE_TOOL_LOG_RAW_PAYLOADS=false`
