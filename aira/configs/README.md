# 配置示例的使用边界

`agent-runtime.example.json` 是远端运行配置的**结构示例**，不是完整启动配置，也不是生产 Nacos 的导出。

1. 替换所有 `<...>`；example.invalid 是故意不可用的示例域名。
2. 启动侧另外配置 appsettings 中 Kestrel 和 Nacos。完整字段结构见上一级的配置字段附录。
3. 使用自己的 Nacos DataId、namespace 和配置值，确认对应环境。不要把文件直接写进现有生产 Nacos。
4. Agent 实例、Prompt、Skill、脚本和绑定另存在 CentraHub，本文件不能创建它们。
5. ADB、TemplateDB、OSS、OAuth、report-validator 按所选能力配置；完整业务仍需租户解析、权限和业务库。
6. ChatHistoryTokenBudget=48000 是根据压缩器默认值给出的显式示例，实际生产值未知，需按模型上下文预算调整。
7. 本例关闭 OpenTelemetry 及消息正文采集，属于独立开发示例选择，不代表原项目默认关闭。
8. IM Gateway 和云文档授权另见第 02、04 章；两种服务的配置大小写、层级不同，不应共用此 JSON。
