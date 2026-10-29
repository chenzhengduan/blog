# Skill、提示词、工具与外部能力

> 2026-10-29 从当前工作区提取。本章描述源码可证明的实现；数据库中的当前提示词正文、版本和启用名单没有读取。配置示例均为占位值。路径相对于 `D:/code/Wtwo-project`。

## 1. 三种容易混淆的配置

| 机制 | 保存位置 | 加载方式 | 实际作用 |
|---|---|---|---|
| 全局/Agent 引导词 | CentraHub 的 `tAgentCfgGlobal`、`tAgentCfgVersion` | 物化为 Prompts/*.md，构建 Agent 时拼接 | 角色、行为、工具选择和输出要求 |
| 平台 Skill | `tAgentCfgSkill`，版本中保存绑定 key | `AgentSkillsProvider` 按需加载正文和资源 | 可复用业务说明、表结构和流程 |
| 用户 Skill | `tAgentUserSkill` | 目录注入 + `load_user_skill` | 用户固化的指令、指标、工作流与定时执行定义 |

工具不是 Skill：`SqlQueryTools`、`CloudDocTools` 等是 C# 代码；Skill 告诉模型何时和如何调用工具。工具组白名单决定能力是否存在，residentTools 决定能力是否常驻，approvalTools 决定写动作前是否需要审批。仅把一篇 Markdown 放进 Docs 不会自动给线上 Agent 新增能力。

## 2. 数据库正本与磁盘物化

### 2.1 配置模型

| 表 | 职责 |
|---|---|
| `tAgentCfgInstance` | Agent 名称、启用状态、激活版本、展示信息 |
| `tAgentCfgVersion` | 模型、工具白名单、常驻组、审批规则、提示词字典、Skill keys、辅助文件 |
| `tAgentCfgSkill` | SkillKey、DisplayName、Description、Content、ResourcesJson、快捷入口和分类 |
| `tAgentCfgSkillBinding` | AgentName、SkillKey、Sort，便于查看共享 Skill 的绑定关系 |
| `tAgentCfgSkillHistory` | Skill 修改前的历史版本 |
| `tAgentCfgGlobal` | 全局配置，已确认键包括 GlobalPrompt、McpHiddenTools |
| `tAgentUserSkill` | 用户技能：公司、用户、名称、command、triggers、content、visibility、status、指标引用、schedule、version |

`AgentConfigRepository.MaterializeAgent` 从启用实例的 ActiveVersionNo 读取版本，写运行 `Instances/<agent>`；将版本 `SkillsJson` 中的 key 找到共享 Skill 正文，写为 `Skills/<key>/SKILL.md` 及 ResourcesJson 的资源。清除已不属于当前版本的旧提示词/Skill，防止停用能力仍残留在目录里。

Agent 构建期调用物化，然后读 `agent.json`。磁盘是框架消费配置的格式，数据库才是维护入口。迁移时需要导出**全局配置 + Agent 激活版本 + 所有绑定 Skill 正文及资源 + 版本辅助文件**，只导出 SKILL.md 不足以复原。宿主在挂载管理路由前就构建 gclaw，因此新环境必须先通过获授权的初始化流程准备可用 gclaw；不能用“先启动空库服务，再打开管理页创建 gclaw”作为现有代码的启动方案。gclaw-workbench 是另外的实例配置。

### 2.2 生效与版本管理

管理 API 位于 `AgentManageController`，服务内部路由是 `/agent/AgentManage/<Action>`，代码注释给出的网关路径为 `/api/agent/AgentManage/<Action>`。它使用 `[NoAuth][NoTenant]` 跳过普通业务鉴权/租户切换，但仍由 `[AgentManageAuth]` 做独立管理鉴权，并不是公开无鉴权接口：

- `SaveVersion`：保存新版本并激活；随后需要 `Reload` 使已构建 Agent 重新创建。
- `ActivateVersion`：选择已有版本；随后需要 Reload。
- `GetSkill` / `GetSkillHistoryDetail` / `SaveSkill` / 回滚相关 action：读写共享 Skill，保存前保留历史。
- 全局提示词保存是整份覆盖，空串会清除全局内容；后续物化不再写 global.md。
- `Reload` 清空当前进程 Agent 缓存。多实例是否全部刷新必须按实际部署入口验证，不能把一次 HTTP 调用自动等同全副本已刷新。

Skill 修订发布字段 `Revision/PublishedRevision` 仅在 Development/dev/Test/testing 的 `AgentSkillPublicationPolicy` 中启用。生产读取有显式 ContentColumns，刻意不依赖这些列。数据库迁移和发布不能假设所有环境字段完全一样。

仓库规定自 2026-10-27 起 `Docs/skill/<key>/` 仅为参考快照，不是维护正本。获授权的维护方式是管理页，或 `Docs/skill/sync-skills.ps1 -Key <key> -Remark <说明>` 单项同步。本文没有运行同步或任何数据库写操作；复刻环境应按管理入口导入，不把正文硬塞进业务 SQL 迁移。

### 2.3 一个最小结构示例

下面表示物化后的 `agent.json`，是复刻结构示例，**不是当前生产配置**：

```json
{
  "name": "gclaw-workbench",
  "displayName": "AIRA",
  "description": "教培机构经营与业务协作助手",
  "model": {
    "deployment": "<主模型>",
    "multimodalDeployment": "<视觉模型>",
    "fallbackDeployment": "<备用模型>"
  },
  "tools": ["BasicDataTools", "SqlQueryTools", "AskUserChoiceTools", "WorkbenchObjectTools", "CloudDocTools"],
  "residentTools": ["BasicDataTools", "AskUserChoiceTools", "WorkbenchObjectTools"],
  "approvalTools": ["CloudDocTools"],
  "skills": ["sql-query-examples"],
  "systemPrompts": ["system.md"]
}
```

采用整组审批会连组内只读函数也被要求审批，实际配置应根据需求选择函数粒度；此示例展示机制，不能直接当作推荐生产策略。框架自动处理按需加载工具组，须以 `Normalize...` 与最终工具快照核对实际清单。

## 3. 系统提示词怎样形成

`AssistantAgentFactory.BuildSystemPrompt` 的拼接顺序：

```text
ChatOptions.Instructions =
    "---\n\n" + Prompts/global.md 的非空全文
  + 按 agent.json.systemPrompts 顺序：
      "---\n\n# " + 文件名去后缀 + "\n\n" + 对应文件全文
```

系统引导文件缺失只记警告并跳过；代码没有内置完整角色提示词回退。`LoadAgentConfig` 的空配置回退也不能当作可用 Agent，因为实例目录与 Skill 目录仍是实际依赖。

每次 completion 还会追加运行时 system 消息，来源见第 03 章的 provider 表。代码中仍存在**动态注入模板与专用辅助任务提示词**，因此“所有提示词都在库里”也不准确：

| 来源 | 代码里的内容 |
|---|---|
| RuntimeContextProvider | 当前时间/用户/机构模板 |
| UiContextProvider | 窗口背景与排课确认指令，防止猜测预览版本 |
| UserSkillContextProvider | 用户技能目录格式与按 ID/指令命中规则 |
| ConversationContextProvider | 工作记忆、恢复任务和不确定匹配说明 |
| SkillAssistService | 帮用户生成/整理 Skill 的辅助模型任务提示词 |
| AgentConversationContextService / CompactionService | 工作记忆结算、摘要与压缩规则 |
| 工具/参数 `[Description]` | 向模型描述功能、入参、权限条件和使用限制 |

附录提供本地快照和代码模板节选。生产 global.md/system.md 不在这次可得证据中，不能用测试夹具伪装成现网原文。

## 4. 平台 Skill 的加载

`AgentSkillsProvider` 从物化后的 Skills 根目录读取。Factory 设置：

```csharp
new AgentSkillsProviderOptions {
    DisableLoadSkillApproval = true,
    DisableReadSkillResourceApproval = true,
    DisableRunSkillScriptApproval = true
}
```

这些开关关闭框架默认的 Skill 读取/脚本审批，避免后台渠道因等待框架审批内容而暂停；它们不等于批准所有业务写操作。业务工具审批由 AIRA 自己的 `ApprovalRequiredAIFunction` 和 Workbench 交互处理。

加载流程：

1. Agent 看到 Skill 目录与简述，判断任务所需 Skill。
2. `load_skill` 取完整 SKILL.md。
3. `read_skill_resource` 取精确资源路径，如 `tables/fee.md`。
4. 模型遵守 Skill 执行 C# 工具；需要更多工具组时调用 `loadTools`。
5. Skill 内脚本执行能力来自 framework provider；业务脚本工具另见第 7 节，两者不能仅凭名字混同。

Skills provider 必须放在 `ChatClientAgentOptions.AIContextProviders`，在 Agent 准备 session 时合并工具。若把它放在更内层 ChatClient 的动态上下文 provider 列表，模型可能看得到 load_skill，但外面的函数调用器找不到可执行函数。

## 5. 用户 Skill 的加载

`UserSkillContextProvider` 从 `IUserSkillService.GetCatalogAsync(companyId,userId)` 读用户可用目录（私有、机构公开、公共市场），注入名称、skillId、精准指令、触发词、描述、指标引用和定时配置，不常驻正文。

用户快捷菜单显式指定 skillId 时按 ID 调用 `load_user_skill`；`skill:<command>` 按精准指令匹配；普通文本可按触发词命中。没有匹配不强行激活。同名 Skill 不能互换；无权读取或失效 ID 必须返回不可用。

用户 Skill 表里 `TriggerType`、`MetricIds`、`Schedule` 是业务工作流配置。定时任务仍进入统一 Turn/调度链，不是让 Markdown 自己开线程。平台 Skill 与用户 Skill 可以共同参与一次任务，但正本、权限、版本和触发入口是两套体系。

## 6. C# 工具装配与按需加载

### 6.1 工具声明和注册

工具类用类级 `[Description]` 进入工具组目录；public 方法与参数的 `[Description]` 供模型理解。`AssistantAgentTools.CreateAIFunctions` 显式登记每个工具组与函数 delegate，通过 `AIFunctionFactory.Create` 生成 JSON schema。新增工具只写 public 方法而不加入组工厂，模型仍调用不到；代码有组/方法清单一致性诊断。

普通业务函数往往去掉 Async 后缀，前端 op 则有自己的内部标识，最终暴露名应以 AIFunction.Name 为准。不要按文档显示名或未经校验的方法名猜协议名。

| 当前注册工具组 | 主要用途 |
|---|---|
| BasicDataTools | 校区、员工、字典 |
| FinanceTools / PerformanceTools / PerformanceCalcTools | 财务、业绩查询与计算 |
| LeadTools / LeadWriteTools | 招生线索查询和动作 |
| StudentTools / StudentOperationTools | 学员查询和运营 |
| FamilySchoolTools / FamilySchoolWriteTools / HomeworkDakaTools | 家校服务与打卡 |
| ScheduleQueryTools / ScheduleWriteTools | 教学统计、排课、调整、补课 |
| ClassTools / ClassWriteTools / ShiftTools / ShiftWriteTools | 班级、课程与写操作 |
| SqlQueryTools / ReportQueryTools | 安全 SQL 和报表查询 |
| OssUploadTools / FileTools / ScriptTools / ChartTools | 文件、脚本、图表与上传 |
| ScheduleTaskTools / MonitorConfigTools / MetricConfigTools | 定时任务、监控、指标口径 |
| CloudDocTools | 飞书与企业微信云文档 |
| ConversationHistoryTools / AskUserChoiceTools / UserSkillTools | 历史、选择题、用户 Skill |
| WorkbenchShellTools / WorkbenchObjectTools | 工作台壳与工作对象 |
| SystemTools / TodoTools / ToolLoaderTools | 基础系统能力、待办和工具组加载 |

这是当前装配能力清单，不代表每个 Agent 全部启用。真实启用以 DB 版本 tools 为准。写工具通常委托模块 Application/Composition，不能将 Skill 改写为绕过业务服务的直接 SQL 写库。

### 6.2 工具包装层

调用的外到内顺序为：审批 → Telemetry → 结果截断 → 业务函数。

- 截断阈值 `AppSettings:ToolResultMaxChars` 默认 12000 字符。
- 日志内容上限 `OpenTelemetry:ContentMaxLength` 默认 8000。
- 审批必须在外层，防止内部遥测把中断信号误处理为普通工具失败。
- Workbench 选择题和工具审批交互有渠道限制。IM 不具备同一套交互卡片恢复流程；需要普通追问或引导用户进入系统完成审批，不能照搬网页审批协议。

### 6.3 residentTools 与 loadTools

按需加载总开关是 `AppSettings:AgentTurn:ToolLoading:Enabled`，缺省 true；Agent residentTools 非空才对常驻清单进行裁剪。未配置 residentTools 表示全部常驻，不是没有工具。

工具目录按 Agent 隔离，前端 App op 清单由 Workbench manifest 上报。Workbench 系列使用每轮运行时快照；普通 Agent 在开启加载机制且配置 residentTools 时也使用运行快照。此类 Agent **构建时 Tools 留空**，Runner 每次执行再注入，避免框架将构建清单与运行清单重复合并。

`loadTools(groupName)` 同时：

1. 验证 groupName 存在于白名单减常驻组的可加载目录。
2. 写入会话已加载登记 `ConversationToolMountService`。
3. 将组工具立即追加到当前 run 的 ChatOptions.Tools，使同一工具循环的下一次模型调用可直接使用。
4. 后续 Turn 合并已加载组，避免重复加载。

登记 Redis key 为 `wb:tool-mount:<conversationId>`、有效期 7 天，本机缓存 60 秒。实现明确区分登记失败与加载成功，不能只返回“加载了”却没有注入当前清单。

## 7. 数据与脚本的执行边界

### 7.1 安全 SQL

`SqlQueryTools.ExecuteSafeQueryAsync` 的注解要求先读 `sql-query-examples`。程序层还检查：只读单条 SELECT、禁写关键字、租户参数 `cCompanyID=@companyId`、校区权限改写、结果手机号权限处理。元数据视图和明确列出的无租户字典表有例外；业务表与例外表混用不免除机构限制。

服务不是简单执行模型原文。它提取表名并校验语法/权限，再替换真实公司参数。默认补 `TOP 201` 用于探测是否超 200 行，对模型只给最多 200 行；超限会尝试归档完整结果为数据集，返回 datasetId/总量供进一步分析和导出。权限不足的手机号应打码，不能把日志/中间结果视为绕过授权的入口。

报表 SQL 另限制日期参数 `@startDate/@endDateExclusive`，用于可重放动态报表；与一次性即席查询不同。

### 7.2 脚本和图表

`ScriptTools.ExecuteAsync` 使用容器内 `Process.Start`：Windows `cmd /c`，Linux `/bin/sh -c`，缺省工作目录是 `Instances/<agent>/workspace`，超时默认 60 秒、范围 1～300 秒，stdout/stderr 各截到 100000 字符。它做工作目录前缀校验，但没有在此类中实现独立操作系统沙盒、文件访问白名单或命令白名单。

因此“sandbox workspace”只能描述运行目录约定，不能宣传为已经实现完善隔离。克隆部署应检查容器用户、卷挂载、网络和进程权限，敏感工具启用范围与业务授权策略分别验证。

镜像构建依赖 `wtwo-server/.deploy/agent-scripts/package.json`：ECharts `^5.6.0`、canvas `^3.2.3`、ExcelJS `^4.4.0`、`@wecom/cli ^1.3.4`、`@larksuite/cli ^1.0.97`。这些是版本范围；实际安装版本以 lock/install 产物为准。运行脚本可能由 Agent 版本 FilesJson 物化，不应假设 npm 清单包含全部运行脚本。

## 8. 云文档接入：飞书与企业微信的不同身份

云文档读写复用机器人应用凭证，但**机器人收发消息成功不等于文档已授权**。统一入口是 `CloudDocTools`：

| 方法 | 用途 |
|---|---|
| `ReadDocAsync(docUrl)` | 读取普通文档/在线表格/多维或智能表格 |
| `CreateDocAsync(title,content,docType,sheetRows,...,platform)` | 新建文档或表格 |
| `AppendDocAsync(docUrl,content)` | 追加正文 |
| `AppendSheetRowsAsync(docUrl,rows,datasetId,startRow,sheetName)` | 追加行，可用 datasetId 直接写入 |
| `RevokeDocAuthorizationAsync(...)` | 解除当前用户的飞书文档授权 |

读取按链接识别平台；创建可显式 platform=feishu/wecom，缺省按渠道判断。飞书文档以授权用户身份创建；企微文档以机器人身份创建，尽量把当前用户加协作者。飞书 Markdown 原样使用；企微正文会转为纯文本。大表超过 200 行时可存成数据集，模型只看到列概况、前 10 行和 datasetId；几千行写回也直接走数据集，避免让 LLM 逐行抄写。

### 8.1 飞书

```text
CloudDocTools
 → LarkCliRunner.ResolveBotAsync（当前会话 / 当前用户 / 当前机构可用飞书机器人）
 → FeishuOAuthService.GetAccessTokenAsync
     ├─ 用户现有 access token 有效：解密使用
     ├─ 可 refresh：刷新并保存
     └─ 未授权：发起/恢复 device authorization，返回用户操作链接
 → 启动 lark-cli 并注入应用与用户令牌
 → 解析 JSON 结果 / 返回缺权限或续期原因
```

服务代码所用端点：

- device authorization：`https://accounts.feishu.cn/oauth/v1/device_authorization`
- token：`https://accounts.feishu.cn/oauth/v3/token`
- revoke：`https://accounts.feishu.cn/oauth/v1/revoke`
- user info：`https://open.feishu.cn/open-apis/authen/v1/user_info`

请求 scope 原文是：

```text
offline_access docx:document docx:document:readonly wiki:wiki:readonly drive:drive sheets:spreadsheet sheets:spreadsheet:read bitable:app
```

本段是项目当前实现清单，不保证平台以后不变。应用后台必须已经申请对应权限；服务返回 `missing_scope/app_scope_not_applied` 时要区分用户授权缺失与应用后台未开权限。表格的 spreadsheet 与 spreadsheet:read 在代码中都申请。

用户第一次调用会拿到 `NeedAuthorize/AuthorizeHint`；点击后回到会话说“继续”，下一次调用再 poll token。待授权状态放分布式缓存；成功令牌存 `tAgentImUserOAuth`，按机构/用户/机器人隔离。access 到期前 5 分钟开始刷新；同进程锁减少重复刷新，跨实例更新考虑 refresh token 被别的实例更新的情况。

`FeishuTokenCipher` 使用 32 字节密钥（配置为 64 位十六进制）和随机 IV 的 AES-CBC/PKCS7，存储格式为 `base64(IV + cipher)`。`AppSettings:CloudDoc:FeishuTokenCipherKey` 缺失返回 cipher_key_missing，不应让用户误去飞书反复授权。更换密钥意味着已有令牌不能解密，需要用户重新授权。

子进程环境变量：

```text
LARKSUITE_CLI_APP_ID
LARKSUITE_CLI_APP_SECRET
LARKSUITE_CLI_USER_ACCESS_TOKEN
LARKSUITE_CLI_CONFIG_DIR
LARKSUITE_CLI_LOG_DIR
LARKSUITE_CLI_NO_UPDATE_NOTIFIER=1
LARKSUITE_CLI_NO_SKILLS_NOTIFIER=1
```

可执行文件优先 `AppSettings:CloudDoc:LarkCliPath`，然后发布根目录 node_modules/.bin，最后 PATH。每次调用独立工作/配置目录，避免不同用户共享 CLI 状态。

### 8.2 企业微信

```text
CloudDocTools
 → WecomCliRunner.ResolveBotAsync
 → 复用 tClawUserBotBind 的 AppID(bot_id) 与 AppSecret(secret)
 → 签名请求 get_cli_config 取得机器人访问令牌
 → WECOM_CLI_ACCESS_TOKEN 注入 wecom-cli
 → 文档 API 结果；必要时返回机器人创建者授权指引
```

机器人选择顺序：当前会话机器人 → 当前用户自己创建的活跃企微机器人 → 当前租户最新可用机器人。不同层级的替代都先按 CompanyID 限定。

请求 `https://qyapi.weixin.qq.com/cgi-bin/aibot/cli/get_cli_config`，JSON 字段为 bot_id、time（Unix 秒）、nonce、signature、bind_source=1。签名是 `SHA256(secret + botid + time + nonce)` 的小写十六进制。此处只说明算法，不复制实际 secret。

令牌在进程内缓存，软过期 12 小时；错误 `853004` 时清缓存并重取一次。`851008` 表示能力未授权，`850003` 表示授权过期，返回 help_message 的最新一次性链接应按工具给出的说明交给机器人创建者。企业微信没有这条链路中的用户级 OAuth，不能让普通会话用户代替创建者完成机器人授权。

子进程用 `WECOM_CLI_ACCESS_TOKEN`、`WECOM_CLI_CONFIG_DIR`、`WECOM_CLI_LOG_DIR`。可执行路径由 `AppSettings:CloudDoc:WecomCliPath` 覆盖，其次 node_modules/.bin 和 PATH。工作/配置目录每次独立。

## 9. MCP：向外部 AI 暴露本项目业务能力

已确认的主要方向是 **AIRA 作为 MCP Server 对外提供工具**，不是这里自动接入任意第三方 MCP 服务器。`McpServerRegistration` 使用 `ModelContextProtocol.AspNetCore` 的 Streamable HTTP，`Stateless=true`；服务路由 `/agent/mcp`，网关环境可能在外侧带 `/api`。

工具来源是 `AssistantAgentTools.CreateMcpAIFunctions`，缺省 AgentName 为 `gclaw`：读取其 DB 白名单，完整暴露白名单工具，排除 loadTools，再按全局 `McpHiddenTools` 过滤。无状态 MCP 不使用 Workbench 的会话按需挂载语义。隐藏清单会规范化 Async 后缀，避免管理页方法名与真实暴露名不匹配。

**复刻注意：**部分方法带 `[McpHidden]` 注解，但当前 `CreateMcpAIFunctions` 的可见过滤代码以全局隐藏名单为关键入口；不能只凭注解就断言对外 tools/list 已隐藏某个敏感工具，应实测清单与拒绝调用。

### 9.1 OAuth 入口

```text
GET  /.well-known/oauth-protected-resource
GET  /.well-known/oauth-authorization-server
POST /oauth/register
GET  /oauth/authorize
GET  /oauth/consent-info
POST /oauth/consent
POST /oauth/token
POST /oauth/revoke
```

接入流程：客户端发现元数据 → 动态注册 redirect URI → 授权码与 PKCE S256 → AIRA 用户登录并确认机构/能力组 → 用 code+verifier 换 token → Bearer 调 MCP。token/revoke 使用 form-urlencoded，不能用项目普通 CallResult 的 JSON 协议替代 OAuth 错误形态。

`OAuthBearerMiddleware` 校验 token 并恢复公司/用户身份，再进入租户和权限中间件。tools/call 还按工具组 scope 拦截，未知工具对受限 scope 拒绝；仅拥有 bearer token 不代表可越权调用所有工具。

`AppSettings:AgentOAuth` 的重要缺省：access 3600 秒、refresh 30 天、authorization code 90 秒、authorize session 600 秒、token context cache 60 秒、DCR true、redirect URI 最多 10、rate limit 每分钟 30。scope 的全部权限标识为 `*`，省略 scope 的代码缺省也是全部，实际接入应审查同意页面展示与最小范围要求。

令牌采用随机 opaque token，数据库存 SHA256 hash；refresh 有令牌族/轮换与撤销处理，授权临时状态在分布式缓存。这里的 OAuth 与飞书用户文档 OAuth 是两套独立授权：一个把 AIRA 工具授予外部客户端，一个让 AIRA 代表用户读写飞书。

## 10. 复刻时应交给 AI 的配置资产

仅有本章和代码仍不足以等价恢复生产。需要部署方在授权范围内补齐：

1. 每个启用 Agent 的当前激活版本（含工具和审批名单）与全局提示词。
2. 所有绑定 Skill 的 Content、ResourcesJson、资源路径和辅助文件；文档附录只提供可确认的本地快照。
3. Nacos 业务配置的脱敏结构、密钥注入方式、模型通道和容量限制。
4. 测试机构/用户/权限/业务表结构与最小数据，不能把公司 ID、用户 ID 交给模型编造。
5. 云文档机器人的应用权限、飞书用户授权、企微机器人创建者授权与 CLI 实际安装版本。
6. MCP 客户端登记、回调地址、网关转发、同意页面与实际 tools/list。

新能力按“业务模块 → C# 工具 → 组工厂注册 → Agent 白名单/审批 → Skill 指引 → 端到端验证”增加。只改提示词不能替代服务实现；只改工具也不会自动启用给所有 Agent。

## 11. 主要源码证据与可复用测试入口

| 内容 | 定位 |
|---|---|
| 配置物化 / 版本保存 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Framework/Manage/AgentConfigRepository.cs:356`、`:653` |
| 管理保存与 Reload | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/AgentManageController.cs:81`、`:160` |
| Skill 表 / 全局表 | `wtwo-server/WTwo.Bus.Common/WTwo.Bus.Common.Model/DBEntity/CentraHub/AgentSkillDO.cs:12`；`AgentGlobalConfigDO.cs:12`（同目录） |
| Skill 发布策略 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Framework/Manage/AgentSkillPublicationPolicy.cs:8` |
| 系统提示词拼接 / Skill provider | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Framework/Assembly/AssistantAgentFactory.cs:195`、`:331` |
| 用户 Skill 目录与规则 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Framework/Providers/UserSkillContextProvider.cs:28` |
| 工具装配与 MCP 暴露 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Framework/Assembly/AssistantAgentTools.cs:222`、`:1080` |
| 工具包装层 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Workbench/Apps/WorkbenchAppOpToolBuilder.cs:54` |
| 当前轮即时加载 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Tools/SystemUtility/ToolLoaderTools.cs:73` |
| 会话加载缓存 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Workbench/ConversationToolMountService.cs:20` |
| SQL 安全与数据集 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Tools/Sql/SqlQueryTools.cs:157`、`:216` |
| shell 实际执行边界 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Tools/SystemUtility/ScriptTools.cs:34` |
| 文档统一入口 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Tools/CloudDoc/CloudDocTools.cs:53` |
| 飞书用户授权 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Tools/CloudDoc/FeishuOAuthService.cs:118` |
| 飞书 CLI / 加密 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Tools/CloudDoc/LarkCliRunner.cs:135`；`FeishuTokenCipher.cs:15`（同目录） |
| 企微签名换 token | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Tools/CloudDoc/WecomCliRunner.cs:167` |
| MCP Server | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/McpServerRegistration.cs:20` |
| OAuth 路由 / scope 拦截 | `wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/OAuth/AgentOAuthEndpoints.cs:25`；`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/WebMiddleware/OAuthBearerMiddleware.cs:88` |
| CLI 依赖声明 | `wtwo-server/.deploy/agent-scripts/package.json:6` |

可复用现有测试文件（位于 `wtwo-server/tests/WTwo.Agent.Tests/`）：`ToolApprovalTests.cs`、`ApprovalInterruptMechanismTests.cs`、`QuickSkillSelectionTests.cs`、`SkillToolTelemetryPipelineTests.cs`、`AgentSkillHistoryTests.cs`、`AgentSkillPublicationTests.cs`、`AgentOAuthServiceTests.cs`、`CloudDocFeishuTests.cs`、`CloudDocWecomCliTests.cs`。本次仅核验源码和文档，未运行这些测试；测试存在不等于外部平台当前集成已验证。

