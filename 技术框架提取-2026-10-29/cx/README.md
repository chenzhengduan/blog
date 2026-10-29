# CX 与 xzagent 技术框架及复刻指南

这套资料解释 CX 教育 AI 平台如何实现出题、出卷、录题、批改、知识点识别、对话式工具调用和飞书 Bot，以及如何从现有代码重建服务。提取时间为 **2026 年 10 月 29 日**，依据 `D:\cx\cx-ai-doc` 与 `D:\cx\xzagent` 当前工作树。

核心设计是两层 Agent：**CX 业务后端内嵌 AgentScope 执行教育任务，xzagent 承载通用对话、渠道、Skill、远程工具和会话运行时**。两者通过 HTTP、JWT 换取和远程工具协议协作，不是一个服务简单代理另一个服务。

## 阅读导航

| 目的 | 文档 |
|---|---|
| 快速讲清项目设计、技术选型与功能 | [01 架构总览与功能地图](01-架构总览与功能地图.md) |
| 看清请求走向、身份、SSE、数据模型与异步任务 | [02 服务调用与业务实现](02-服务调用与业务实现.md) |
| 详细理解飞书 Bot，以及企业微信当前缺口 | [03 Bot 接入与消息链路](03-Bot接入与消息链路.md) |
| 理解模型选型、模型协议、路由和本地 YOLO | [04 模型接入与本地推理](04-模型接入与本地推理.md) |
| 理解通用 Agent、AG-UI、A2UI、A2A、分布式会话 | [05 xzagent 运行时与编排](05-xzagent运行时与编排.md) |
| 复用提示词、Skill、Bundle 和工具绑定 | [06 Skill 与提示词工程](06-Skill与提示词工程.md) |
| 配环境、建库、启动与部署 | [07 配置部署与运维](07-配置部署与运维.md) |
| 交给 AI 按阶段重建和验收 | [08 AI 复刻执行手册](08-AI复刻执行手册.md) |
| 回答项目技术问题、避免历史资料误导 | [09 项目问答与证据边界](09-项目问答与证据边界.md) |
| 定位接口源码 | [10 HTTP 路由源码索引](10-HTTP路由源码索引.md) |
| 查环境变量位置 | [11 配置变量索引](11-配置变量索引.md) |

## 可直接阅读和改造的附件

- [assets/config](assets/config)：四个服务的启动配置和 Nacos 模板脱敏副本。保持配置结构，清除凭证默认值，部署地址改为示例地址。
- [assets/prompts](assets/prompts)：业务 Agent system/user prompt 全量文本，包含历史版本与当前版本；实际生效版本按正文和加载代码判断。
- [assets/backend-skills](assets/backend-skills)：CX classpath Skill 与学科参考提示词。
- [assets/cx-bundle](assets/cx-bundle)：CX 对接 xzagent 的 Bundle 内容、Skill、远程工具 Meta、A2UI Catalog 样例；环境地址需重新填写。
- [assets/database/cx-schema.sql](assets/database/cx-schema.sql)：CX 当前 PostgreSQL 全量 DDL；仅用于新建空库，已有库应走迁移。
- [assets/local-model](assets/local-model)：本地 YOLO 权重清单与 CPU 依赖锁文件，不含模型权重二进制。
- [assets/xzagent-api-notes](assets/xzagent-api-notes)：xzagent 源仓库中的 API 分区与接入说明快照；当前实现仍以 Controller 为准。
- [asset-manifest.json](asset-manifest.json)：附件来源、是否脱敏、源文件及产物 SHA-256。
- [source-snapshot.json](source-snapshot.json)：仓库快照信息。部分 Git 元数据受本机访问权限限制，不能读取的字段标记 unavailable；提取内容来自可读工作树，不保证等同某个发布版。
- [validation-report.json](validation-report.json)：正文链接、源码定位、附件哈希、JSON结构及敏感格式检查结果；检查范围与未执行的联调项目一并记录。

## 使用约定

文中 `cx-backend/...:123` 相对 `D:\cx\cx-ai-doc`，`xzagent/...:123` 相对 `D:\cx`；行号对应本次快照。附件使用相对链接，搬动整个 `cx` 目录后仍可阅读。

“已实现”指能从代码定位到实现；“模板示例”不等同线上配置；“建议复刻步骤”是据此整理的执行顺序。没有读取生产 Nacos、数据库或线上实例，也没有在本次提取中启动整套系统。

复刻仍需准备模型服务凭证、飞书应用权限、对象存储、数据库种子数据和 **xzagent 当前正式 DDL**。本机 xzagent 约定的 `D:\cx\db` 未找到，测试 SQL 不能冒充生产初始化脚本。当前实现可确认飞书 Bot，未发现企业微信 Bot 适配代码。

已定位的接口兼容缺口也在专题记录：例如录题取消工具声明DELETE，而当前xzagent远程HTTP适配器只实现GET/POST。本次只提取和核验文档，没有修复业务实现。
