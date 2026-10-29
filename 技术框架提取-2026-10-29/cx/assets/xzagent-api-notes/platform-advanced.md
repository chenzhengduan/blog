高级接入能力，包括 MCP、远程来源、第三方凭证、渠道绑定和工具产物访问等。

普通 Skill 接入方无需关注本分组。

MCP服务`authMode`可为`SERVER`或`USER_CREDENTIAL`，缺失/null默认`SERVER`。用户模式仅支持HTTP/SSE。
当前用户通过`/xzagent/api/user/me/mcp-credential-bindings`查询绑定、PUT `/{serverId}`保存`{"credentialId":"自己的凭证标识"}`、DELETE `/{serverId}`解除绑定。
查询返回裸数组，保存返回serverId/credentialId裸对象；删除不存在的绑定仍返回200空响应。绑定不返回认证头。
凭证必须属于当前用户、应用和租户，启用且未过期，包含合法非空认证头。协议控制头与换行值不允许；同名业务头按大小写不敏感以用户值覆盖公共头。
目标或模式变更后需重新绑定。常见错误为400参数非法、404来源/凭证不可见、409模式或凭证不可用、503暂时无法确认授权；第三方认证失败不表示平台登录失效。
MCP OAuth编排与自动刷新暂不支持；Bundle不包含最终用户密钥或个人绑定。
