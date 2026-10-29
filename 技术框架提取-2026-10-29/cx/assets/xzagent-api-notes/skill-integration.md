面向普通接入方研发的最小主流程接口。

> 想用 Coding Agent（Cursor / Claude Code 等）快速对接 xzagent？xzagent 提供配套的开发者 skills（xzagent-bundle / xzagent-meta / xzagent-tool-skill / xzagent-frontend / xzagent-remote-auth），加载后即可让 AI 按平台规范产出对接代码与产物。请联系 xzagent 团队获取。

## 推荐对接流程

1. 获取 App JWT（`POST /xzagent/api/auth/app-token`）
2. 为租户内业务用户签发 User JWT（`POST /xzagent/api/auth/user-token`；如需同时写入第三方凭证并绑定远程来源，可改用一体登录绑定接口 `POST /xzagent/api/tenant/auth/user-login-bind`）
3. 调用 AG-UI run 接口发起运行，以 SSE 接收流式事件（`POST /xzagent/api/user/agui/run`）
4. 断线后通过 `GET /xzagent/api/user/agui/sessions/{sessionId}/stream` 重连补流；通过 `POST /xzagent/api/user/agui/sessions/{sessionId}/cancel` 取消运行
5. 遇到 A2UI 挂起交互时，通过 `POST /xzagent/api/user/a2ui/actions/submit` 提交用户动作恢复运行
6. 通过 `GET /xzagent/api/user/sessions?agentId={agentId}` 分页查询会话列表，通过 `PUT /xzagent/api/user/sessions/{sessionId}/title` 更新标题；通过 `GET /xzagent/api/user/sessions/{sessionId}` 查询会话历史，或 `DELETE` 重置会话

Skill Package 上传与 Agent 配置属于应用管理动作，请使用「App 管理 API」分组中的对应接口。

## 消息渠道接入

渠道是用户进入会话的入口，绑定归属于当前登录用户。接入方应携带 User JWT 调用 `GET /xzagent/api/user/me/channels` 查询该用户已开通的渠道；接口返回跨渠道统一脱敏视图，不包含渠道密钥。

飞书渠道通过 `POST /xzagent/api/user/me/channels/feishu/bindings/start` 发起扫码开通。接口以 `text/event-stream` 返回唯一一个 `qrcode` 事件，事件 `data` 是二维码 URL；扫码后可查询 `GET /xzagent/api/user/me/channels/feishu/bindings`，确认绑定状态为 `active`。

旧 `/xzagent/api/app/channels` 与 `/xzagent/api/app/channel/feishu/**` 管理路径不再提供。绑定所有者仅由 User JWT 确定，接口不接受 `tenantKey` 或用户标识来指定、代管或转让归属。

## 远程工具 OAuth 授权流程

远程工具调用接入方自有接口时，通常需要识别"当前是哪个接入方用户在操作"。这类来源声明了用户级 OAuth 授权（authorization code 流程），接入方按以下流程对接：

> 接入方后端需实现授权端点与 Token 端点（OAuth2 AS 角色），实现要求与检查清单见 `xzagent-remote-auth` skill；meta.json 声明 authFlow 见 `xzagent-meta` skill，clientSecret 放置见 `xzagent-bundle` skill，前端授权卡片见 `xzagent-frontend` skill。

1. Agent 运行中调用到需要授权的远程工具且当前用户无有效凭证时，运行事件流会下发 `CUSTOM(name=auth_required)` 授权事件，`value.authorizeUrl` 即授权链接，`value.message` 为用户提示，随后本轮对话正常结束
2. 接入方前端识别该事件并渲染授权入口（授权卡片或按钮），引导用户在浏览器打开授权链接。授权页是接入方自己的页面（授权服务器即接入方后端），用户在页面完成登录并确认授权
3. 第三方授权完成后 302 回调 `GET /xzagent/api/oauth/callback`，平台自动用授权码换取凭证并完成用户级绑定。该端点由用户浏览器跳转触发，接入方无需主动调用，也不需要在请求中携带 JWT
4. 用户回到对话发送任意消息，工具调用会携带已授权凭证自动重试

凭证临近过期时平台会自动刷新；刷新失败时会重新下发 `auth_required` 事件引导用户再次授权。

## 第三方凭证过期与主动更新

接入方提供的第三方凭证（API Key、长期 token 等）若带过期时间，又不希望失效影响终端用户体验，有两种做法：

1. **主动更新凭证**：接入方服务端在凭证临期前，调用 `PUT /xzagent/api/user/me/third-party-credentials/{credentialId}` 覆盖更新（需 User JWT）。也可重复调用一体登录绑定接口 `POST /xzagent/api/tenant/auth/user-login-bind`，其内部会对同名 `thirdPartyCredentialId` 覆盖写入
2. **提供永久凭证**：接入方直接下发不过期的凭证，省去刷新维护成本（适用于接入方自身受控的长期令牌场景）

OAuth 类来源由平台自动刷新 token；本节针对的是非 OAuth 或无 refresh token 的静态凭证。同一凭证分组（credentialGroup）的 source 共享一份凭证，OAuth 授权一次全组生效。

## 安全边界

appSecret 只能保存在接入方服务端，不能下发到浏览器或移动端。终端用户侧只允许携带 User JWT。

## 应用身份查询会话历史

这两个只读接口由接入方服务端使用普通 App JWT 调用，当前应用必须存在且启用。Tenant JWT、User JWT、体验凭证及 A2A API Key 不能替代 App JWT；应用范围来自认证结果，不能传入 `appId` 指定其他应用。原用户身份的历史接口保持原有权限。

- `GET /xzagent/api/app/sessions`：`tenantKey`、`tenantUserId`、`agentId` 全部必填，按会话归属用户筛选；群聊参与者不等同于归属用户。`page` 默认1，`size` 默认20、范围1～100，按会话更新时间和会话标识降序。未知用户返回空列表，不创建用户；Agent停用不影响历史读取。响应沿用 `items/total/page/size`，`total` 是十进制字符串，列表时间为13位 Unix Epoch 毫秒字符串；更新时间不等同于精确最后用户发言时间，活跃变化时不承诺跨页稳定。
- `GET /xzagent/api/app/sessions/{sessionId}/messages`：已知本应用会话即可直接查询，无需先获取列表或换取用户凭证。首次可传 `afterMessageId`（排除该消息）、`startedAtFrom`（包含）、`startedAtTo`（不包含）及 `limit`（默认20、1～100）。时间必须为带时区的 ISO8601、最多微秒精度；URL中的 `+` 应编码为 `%2B`，双边界必须先后有序。时间与位置范围取交集，按消息顺序升序返回；开始时间未知的消息仅在没有时间条件时匹配。

消息项返回 `id/runId/role/content/startedAt/finishedAt/createdAt/updatedAt`。`runId` 可以为空；返回完整已保存可见正文，正文为空的消息仍保留，不展开卡片、附件、内部提示或原始工具结果。消息四时间为 ISO8601 UTC 字符串，与列表时间格式不同：

- 用户消息的开始时间和结束时间均为该条输入被平台完整接受的时刻。
- 助手消息开始时间为触发请求被平台接受的时刻，包含排队和处理等待；结束时间为该条消息实际结束或明确中断的时刻，不表示成功、交付或已读。同请求的多条回复共享起点，各自结束；有效恢复请求产生的新消息使用恢复请求起点。
- 创建时间是首次建立记录的时刻，更新时间仅在实际记录变化时刷新；已确定的业务时间不因重复同步改写，未知业务时间可以为空。存量消息的开始、结束和创建时间以其原更新时间近似，不能作为还原的精确历史时刻。

消息页返回 `sessionId/upperMessageId/items/hasMore/nextCursor`，不返回总数。有后续页时，下一请求只传同一路径及原样 `cursor`，不能重复传入 `limit`、起点或时间条件。首页确定全会话已保存可见消息的上界，后续追加消息不会混入本轮；读取结束后可用 `upperMessageId` 作为下一轮的 `afterMessageId`。空会话上界为空，非空但筛选无匹配时仍返回真实上界。

固定上界不冻结已有正文或时间补齐，不自动重发已读消息的变更，也不保证正在生成的内容已经保存。分页限制条数，不承诺固定响应字节数。会话删除后停止使用旧位置，新会话使用新标识。

错误行为：参数或继续凭据非法返回400（`APP_HISTORY_QUERY_INVALID` / `APP_HISTORY_CURSOR_INVALID`）；未认证401、不支持的身份或应用禁用403；会话不存在及跨应用查询均404（`APP_HISTORY_SESSION_NOT_FOUND`），不区分资源是否存在；可见会话的必要消息位置失效409（`APP_HISTORY_POSITION_INVALID`），不会静默重头读取；历史存储或授权状态暂不可用返回503，不会伪装成成功空列表。参数重复或显式提供 `appId` 也会被拒绝。
